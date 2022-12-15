package com.wei.gulimall.search.service;

import com.wei.gulimall.search.vo.SearchParam;
import com.wei.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
