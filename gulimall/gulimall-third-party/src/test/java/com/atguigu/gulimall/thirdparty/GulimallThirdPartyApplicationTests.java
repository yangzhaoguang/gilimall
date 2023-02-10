package com.atguigu.gulimall.thirdparty;



import com.aliyun.oss.OSS;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gulimall.thirdparty.component.SmsSendComponent;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSS ossClient;
    @Autowired
    private SmsSendComponent sendComponent;

    @Test
  public   void contextLoads() throws FileNotFoundException {
            ossClient.putObject("gulimall-bucket-2022", "5b5e74d0978360a1.jpg", new FileInputStream("C:\\Java\\java_notes\\其他\\project\\谷粒商城\\资料\\docs\\pics\\5b5e74d0978360a1.jpg"));
        }

        @Test
    public void test() throws Exception {
            System.out.println(sendComponent.createClient());
            sendComponent.sendCode("15511513972","8888");
        }
    }