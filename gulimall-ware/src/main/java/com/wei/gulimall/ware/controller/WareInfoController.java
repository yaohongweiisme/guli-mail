package com.wei.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wei.gulimall.ware.vo.HasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wei.gulimall.ware.entity.WareInfoEntity;
import com.wei.gulimall.ware.service.WareInfoService;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.R;



/**
 * 仓库信息
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 16:07:55
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    //查询sku是否有库存
    @PostMapping("/hasStock")
    public List<HasStockVo> getSkuStock(@RequestBody List<Long> skuIds){
        //HasStockVo:   skuId,hasStock
        List<HasStockVo> vos= wareInfoService.getSkuStock(skuIds);
        return vos;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
