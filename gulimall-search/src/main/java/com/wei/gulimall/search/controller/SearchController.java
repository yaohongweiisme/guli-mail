package com.wei.gulimall.search.controller;

import com.wei.gulimall.search.service.MallSearchService;
import com.wei.gulimall.search.vo.SearchParam;
import com.wei.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("list.html")
    public String listPage(SearchParam param, Model model) {
         SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
