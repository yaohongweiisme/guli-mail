package com.wei.gulimall.product.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class AttrRespVo  extends AttrVo implements Serializable{

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}
