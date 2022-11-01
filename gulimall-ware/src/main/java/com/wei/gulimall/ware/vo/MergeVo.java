package com.wei.gulimall.ware.vo;

import lombok.Data;

import java.util.List;
@Data
public class MergeVo {
    private Long purchaseId;    //采购单Id
    private List<Long> items;       //采购需求Id
}
