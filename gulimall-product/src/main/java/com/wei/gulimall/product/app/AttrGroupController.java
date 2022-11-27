package com.wei.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wei.gulimall.product.entity.AttrEntity;
import com.wei.gulimall.product.service.AttrAttrgroupRelationService;
import com.wei.gulimall.product.service.AttrService;
import com.wei.gulimall.product.service.CategoryService;
import com.wei.gulimall.product.vo.AttrGroupAndAttrsVo;
import com.wei.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wei.gulimall.product.entity.AttrGroupEntity;
import com.wei.gulimall.product.service.AttrGroupService;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.R;


/**
 * 属性分组
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 12:23:49
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    //根据分类查出基本分组和分组旗下的所有属性
    //product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getArrGroupAndAttrs(@PathVariable ("catelogId") Long catelogId){
      List<AttrGroupAndAttrsVo>  vos = attrGroupService.getAttrGroupAndAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }


    //添加属性与属性分组的关联信息
    //product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }



    //删除属性时，删除属性相关联的分类
    //product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteAttrRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrGroupService.deleteRelation(vos);
        return R.ok();
    }


    //获取该属性分组下的所有属性
    //product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R getRelatedAttr(@PathVariable Long attrgroupId) {
        List<AttrEntity>  attrEntities =attrService.getRelatedAttr(attrgroupId);
        return R.ok().put("data",attrEntities);
    }

    ///product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getNoRelatedAttr(@PathVariable Long attrgroupId
            ,@RequestParam Map<String, Object> params) {
        PageUtils page=attrService.getNoRelatedAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
