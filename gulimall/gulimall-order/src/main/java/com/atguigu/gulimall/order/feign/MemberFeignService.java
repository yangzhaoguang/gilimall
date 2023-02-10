package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2023/2/1 15:39
 * Description: 
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /*
     * 获取用户的收货地址
     * */
    @GetMapping("member/memberreceiveaddress/getMemberAddresses/{memberId}")
    public List<MemberAddressVo> getMemberAddresses(@PathVariable("memberId") Long memberId);

}
