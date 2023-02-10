package com.atguigu.gulimall.auth.server;



import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// @SpringBootTest
public class GulimallAuthServerApplicationTests {

    @Test
    public void contextLoads() {
        // MD5 加密
        // 81dc9bdb52d04dc20036dbd8313ed055
        // 81dc9bdb52d04dc20036dbd8313ed055
        String s = DigestUtils.md5Hex("1234");
        System.out.println(s);

        // 使用 盐 加密
        // $1$1234$BdIMOAWFOV2AQlLsrN/Sw.
        // $1$1234$BdIMOAWFOV2AQlLsrN/Sw.
        String s1 = Md5Crypt.md5Crypt("1234".getBytes(), "$1$1234");
        System.out.println(s1);

        // Spring提供的的 MD5+随机盐
        // $2a$10$xSB5/afaoQBDdoEHFcGR4eAE0w./Tgj99e0GssBk4dwLFh7/310UK
        // $2a$10$2ZUEuO15/0tx7tEFSdgdnu/jFhwmWB9FQF.ZRb3Ui9hziTCgTqLgS
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 随机盐加密
        String encode = encoder.encode("123456");
        System.out.println(encode);

        // 判断加密后是否相等
        boolean matches = encoder.matches("123456", "$2a$10$2ZUEuO15/0tx7tEFSdgdnu/jFhwmWB9FQF.ZRb3Ui9hziTCgTqLgS");
        System.out.println(matches);


    }

}
