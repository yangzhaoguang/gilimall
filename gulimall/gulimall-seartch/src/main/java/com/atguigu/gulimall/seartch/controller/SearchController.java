package com.atguigu.gulimall.seartch.controller;

import com.atguigu.gulimall.seartch.service.MallSearchService;
import com.atguigu.gulimall.seartch.vo.SearchParamVo;
import com.atguigu.gulimall.seartch.vo.SearchResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Author: YZG
 * Date: 2023/1/21 18:33
 * Description: 
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * @description
     * @date 2023/1/21 23:02
     * @param searchParam 将页面中的搜索条件自动封装在里面
     * @return java.lang.String
     */
    @GetMapping("/list.html")
    public String toList(SearchParamVo searchParam, Model model, HttpServletRequest request) {
        // 获取URL中的参数字符串
        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);
        // 查询
        SearchResultVo resultVo = mallSearchService.search(searchParam);
        model.addAttribute("result",resultVo);
        return "list";
    }
}
