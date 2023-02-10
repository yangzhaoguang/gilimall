package com.atguigu.gulimall.seartch.service;

import com.atguigu.gulimall.seartch.vo.SearchParamVo;
import com.atguigu.gulimall.seartch.vo.SearchResultVo;

/**
 *
 * Author: YZG
 * Date: 2023/1/21 22:58
 * Description: 
 */
public interface MallSearchService {

    SearchResultVo search(SearchParamVo searchParam);
}
