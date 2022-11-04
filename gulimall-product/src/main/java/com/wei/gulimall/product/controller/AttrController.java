package com.wei.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wei.gulimall.product.entity.ProductAttrValueEntity;
import com.wei.gulimall.product.service.ProductAttrValueService;
import com.wei.gulimall.product.vo.AttrRespVo;
import com.wei.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wei.gulimall.product.entity.AttrEntity;
import com.wei.gulimall.product.service.AttrService;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.R;



/**
 * 商品属性
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-14 10:58:45
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;

    /**
     * 通过spuId回显商品属性
     * @param spuId
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrList(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities=productAttrValueService.baseAttrListForSpu(spuId);

        return R.ok().put("data",entities);
    }


    /**
    通过分类id查询所有属性,分为基础属性和销售属性
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType)
    {   PageUtils page =attrService.queryBaseAttrPage(params,catelogId,attrType);
        return R.ok().put("page", page);
    }



    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo respVo = attrService.getAttrDetail(attrId);

        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttrDetail(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attrVo){
		attrService.updateAttrDetail(attrVo);

        return R.ok();
    }

    /**
     * 更新某个spu的各属性值
     * @param spuId
     * @param entities
     * @return
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttrValue(spuId,entities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
