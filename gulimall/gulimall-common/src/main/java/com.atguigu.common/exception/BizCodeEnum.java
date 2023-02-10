package com.atguigu.common.exception;
/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为 5 为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。
 * 		例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 * 	001：参数格式校验
 * 	002 验证码发送频率太高
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 * 15: 用户
 * 21: 库存
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"未知的系统异常"),
    VALID_EXCEPTION(10001,"数据校验异常"),
    VALID_CODE_EXCEPTION(10002,"验证码发送频率太高，请稍后再试"),
    TOO_MANY_REQUEST(10003,"服务器繁忙，请稍后再试"),
    PRODUCT_UP(11000,"商品上架异常"),
    USERNAME_EXIST_EXCEPTION(15001,"用户名已存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号已存在"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003,"用户名或密码错误");

    private Integer code ;
    private String message;

    private BizCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
