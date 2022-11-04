package com.wei.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id; //采购单ID
    @NotNull
    private List<PurchaseItemDoneVo> items; //采购需求物品

}
