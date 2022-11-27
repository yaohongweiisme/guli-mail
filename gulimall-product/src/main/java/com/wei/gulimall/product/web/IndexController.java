package com.wei.gulimall.product.web;

import com.wei.gulimall.product.entity.CategoryEntity;
import com.wei.gulimall.product.service.CategoryService;
import com.wei.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        // 查出所有的1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevelOne();
        model.addAttribute("categorys",categoryEntities);
        return "index"; //视图解析器自动加上前缀，thymeleaf默认是classpath:/template
    }
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> catalogMap  =categoryService.getCatalogLevelTwoAndThreeJson();
        return catalogMap;
    }
}
