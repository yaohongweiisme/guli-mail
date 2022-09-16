package com.wei.gulimall.product.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttrGroupRelationVo implements Serializable {
    private Long attrId;
    private Long attrGroupId;
}
