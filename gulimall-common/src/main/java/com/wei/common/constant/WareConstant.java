package com.wei.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        RECEIVED(2,"已领取"),FINISH(3,"已完成"),
        FAIL(4,"采购失败");
        private Integer code;
        private String msg;

        PurchaseStatusEnum(Integer code, String msg) {
            this.code=code;
            this.msg=msg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        PURCHASING(2,"正在采购"),FINISH(3,"已完成"),
        ERROR(4,"采购失败");
        private Integer code;
        private String msg;

        PurchaseDetailStatusEnum(Integer code, String msg) {
            this.code=code;
            this.msg=msg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

}
