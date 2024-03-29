package com.atguigu.gulimall.thirdparty.controller;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Author: YZG
 * Date: 2022/12/28 19:30
 * Description: 
 */
@RestController
public class OssController {

    @Autowired
    private OSS ossClient;
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;


    @RequestMapping("/oss/policy")
    public R policy() {

        // 填写Host地址，格式为https://bucketname.endpoint。
        String host = "https://" + bucket + "." + endpoint;

        // 设置上传回调URL，即回调服务器地址，用于处理应用服务器与OSS之间的通信。OSS会在文件上传完成后，把文件上传信息通过此回调URL发送给应用服务器。
        // String callbackUrl = "https://192.168.0.0:8888";

        // 设置上传到OSS文件的前缀，可置空此项。置空后，文件将上传至Bucket的根目录下。
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = date + "/";

        Map<String, String> respMap = null;
        try {
            long expireTime = 300;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessId", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));

            // JSONObject jasonCallback = new JSONObject();
            // jasonCallback.put("callbackUrl", callbackUrl);
            // jasonCallback.put("callbackBody",
            //         "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
            // jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
            // String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
            // respMap.put("callback", base64CallbackBody);
            //
            // JSONObject ja1 = JSONObject.fromObject(respMap);
            // // System.out.println(ja1.toString());
            // response.setHeader("Access-Control-Allow-Origin", "*");
            // response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            // response(request, response, ja1.toString());

        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }
        return R.ok().put("data",respMap);
    }
}
