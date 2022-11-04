package com.wei.gulimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVo {
    //{itemId:xxx,status:xxx,reason:" xxx "}
    private Long itemId;
    private Integer status;
    private String reason;

}
