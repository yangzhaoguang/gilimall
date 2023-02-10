package com.atguigu.common.constant;

/**
 *
 * Author: YZG
 * Date: 2023/1/3 15:53
 * Description: 
 */
public class ProductConstant {

    public enum ProductEnum{
        ATTR_TYPE_BASE(1,"基础属性"),
        ATTR_TYPE_SALE(0,"销售属性");

        private int code ;
        private String msg ;

        ProductEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum ProductPublishStatusEnum{
        NEW_SPU(0,"新建"),
        UP_SPU(1,"上架"),
        DOWN_SPU(2,"下架");

        private int code ;
        private String msg ;

        ProductPublishStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
