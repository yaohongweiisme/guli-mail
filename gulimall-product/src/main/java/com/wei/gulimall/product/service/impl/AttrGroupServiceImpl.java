package com.wei.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.wei.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wei.gulimall.product.entity.AttrEntity;
import com.wei.gulimall.product.service.AttrService;
import com.wei.gulimall.product.vo.AttrGroupAndAttrsVo;
import com.wei.gulimall.product.vo.AttrGroupRelationVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.product.dao.AttrGroupDao;
import com.wei.gulimall.product.entity.AttrGroupEntity;
import com.wei.gulimall.product.service.AttrGroupService;

@Slf4j
@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        //接收检索字段
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(catelogId != 0, "catelog_id", catelogId);
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);

    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.stream(vos).map(
                vo -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(vo, relationEntity);
                    return relationEntity;
                }
        ).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationEntities);
    }

    @Override
    public List<AttrGroupAndAttrsVo> getAttrGroupAndAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = list(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(catelogId != 0, AttrGroupEntity::getCatelogId, catelogId));
        List<AttrGroupAndAttrsVo> vos = attrGroupEntities.stream().map(group -> {
            AttrGroupAndAttrsVo vo = new AttrGroupAndAttrsVo();
            BeanUtils.copyProperties(group, vo);
            //设置各自分类旗下的属性
            List<AttrEntity> relatedAttrs = attrService.getRelatedAttr(group.getAttrGroupId());
            vo.setAttrs(relatedAttrs);
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

}