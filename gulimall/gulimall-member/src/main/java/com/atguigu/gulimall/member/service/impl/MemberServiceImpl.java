package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Member;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void regist(MemberRegistVo vo) {

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setCreateTime(new Date());
        // 设置会员默认等级
        MemberLevelEntity defaultLevel = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        memberEntity.setLevelId(defaultLevel.getId());

        // 设置用户名和密码之前，判断用户名、手机是否重复
        // 如果重复抛异常执行终止
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setNickname(vo.getUserName());
        // TODO 设置用户密码，但是不能简单的直接将密码保存到数据库需要进行加密处理
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        // 加密
        String password = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(password);

        this.save(memberEntity);
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            // 用户名重复，抛出异常
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            // 手机号重复，抛出异常
            throw new PhoneExistException();
        }
    }


    @Override
    public MemberEntity login(MemberLoginVo vo) {

        String loginacct = vo.getLoginacct();
        //123456
        String password = vo.getPassword();

        MemberEntity memberEntity =
                this.getOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (memberEntity != null) {
            // 比对密码
            // $2a$10$kNI1IHKgY7cG1KAJqEqaV.pwVSRQ9tHxbTHBlFnbxyIWWXEH24PMO
            String passwordDB = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, passwordDB);

            return matches ? memberEntity : null;
        }
        return null;
    }

    @Override
    public MemberEntity socialLogin(SocialUserVo vo) {
        // 查询是否是第一次登录。根据uid查询， uid是永久不变的
        MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", vo.getUid()));
        if (member == null) {
            member = new MemberEntity();
            //  如果是第一次登录，就将用户信息保存到数据库中
            // 查询用户信息
            HashMap<String, String> map = new HashMap<>();
            map.put("access_token",vo.getAccess_token());
            map.put("uid",vo.getUid());
            try {
                // 发送请求
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(),map);
                // TODO:获取用户信息
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                // 获取昵称
                String name = (String) jsonObject.get("name");
                // 性别
                String gender = (String) jsonObject.get("gender");
                // 省份
                String location = (String) jsonObject.get("location");
                member.setNickname(name);
                member.setCreateTime(new Date());
                member.setGender("f".equalsIgnoreCase(gender) ? 0: 1);
                member.setCity(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
            member.setSocialUid(vo.getUid());
            member.setAccessToken(vo.getAccess_token());
            member.setExpiresIn(vo.getExpires_in());
            // 创建用户
            this.save(member);
            return member;
        }else {
            //  如果不是第一次登录，就更新数据库中的用户信息
            // 主要就是更新访问令牌和失效时间，因为 uid 是永久不变的
            member.setAccessToken(vo.getAccess_token());
            member.setExpiresIn(vo.getExpires_in());
            this.updateById(member);
            return  member;
        }
    }
}