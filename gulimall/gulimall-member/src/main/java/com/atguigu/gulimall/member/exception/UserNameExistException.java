package com.atguigu.gulimall.member.exception;

/**
 *
 * Author: YZG
 * Date: 2023/1/27 21:09
 * Description: 用户名重复异常
 */
public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名重复");
    }
}
