package com.wei.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wei.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wei.gulimall.product.entity.CategoryBrandRelationEntity;
import com.wei.gulimall.product.service.CategoryBrandRelationService;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 12:23:49
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    /*
    通过分类Id获取旗下的品牌信息
     */
    //product/categorybrandrelation/brands/list
    @GetMapping("/brands/list")
    public R findBrandWhichIsRelativeToCategory(
            @RequestParam(value = "catId") Long catId){
        List<BrandVo>  brandVos =categoryBrandRelationService.getBrandsByCatId(catId);
        return R.ok().put("data",brandVos);
    }
    /*
    获取品牌关联的分类
     */
    @GetMapping("/catelog/list")
    public R findCategoryByBrandId(@RequestParam("brandId")Long brandId){
        List<CategoryBrandRelationEntity> relationEntities = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return R.ok().put("data",relationEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
