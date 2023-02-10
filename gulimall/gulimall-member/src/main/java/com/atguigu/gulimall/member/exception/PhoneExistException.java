package com.atguigu.gulimall.member.exception;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 21:10
 * Description: 手机号重复
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号重复");
    }
}
