package com.atguigu.common.constant;


import org.jetbrains.annotations.Contract;

/**
 *
 * Author: YZG
 * Date: 2023/1/7 13:37
 * Description: 
 */

public class WareConstant {
    // 采购单状态

    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),
        ASSIGNEE(1,"已分配"),
        RECEIVE(2,"已领取"),
        FINISHED(3,"已完成"),
        HASERROR(4,"有异常");



        private int code ;
        private String msg ;

        PurchaseStatusEnum(int code, String msg) {
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

    // 采购需求状态
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),
        ASSIGNEE(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISHED(3,"已完成"),
        HASERROR(4,"采购失败");



        private int code ;
        private String msg ;

        PurchaseDetailStatusEnum(int code, String msg) {
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
