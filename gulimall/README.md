# 谷粒商城

> 视频教程： [【Java项目《谷粒商城》Java架构师 | 微服务 | 大型电商项目】](https://www.bilibili.com/video/BV1np4y1C7Yf/?p=102&vd_source=0766908fb3d9c2986e6c1ab241164fb3)



## 一、项目简介

谷粒商城是一个基于B2C模式的大型分布式电商项目，采用前后端分离开发，后端基于 SpringBoot + Cloud + Cloud Alibaba+ MP + Docker 实现，前端基于 Element-UI 、Vue  + Thymeleaf 实现。

## 二、项目架构

使用 Nginx作为反向代理服务器，将客户端的请求转发到 GateWay 网关，有网关负载均衡到 各个的微服务。

使用 Sentinel 对网关或者服务进行熔断限流，使用 Nacos 作为配置、注册中心，使用 OpenFeign 进行远程调用。

缓存层使用 Redis、持久层使用MYSQL，后期使用 ShardingSphere 做 MYSQL 集群、消息队列使用 RabbitMQ，使用 ElasticSearch 作为检索工具。

第三方服务使用了阿里云的OSS对象存储、阿里云云短信、以及微博的社交登录

![image](https://user-images.githubusercontent.com/102777678/218044427-ba817a19-c691-4b53-a64f-79cb39b2c1ae.png)



## 三、项目微服务划分



项目包括：商品服务、优惠服务、仓库服务、订单服务、中央认证服务、支付服务、用户服务、秒杀服务、检索服务、购物车服务、第三方服务，总共11个服务

```
gulimall
├── gulimall-common -- 工具类及通用代码
├── renren-generator -- 人人开源项目的代码生成器
├── gulimall-auth-server -- 认证中心（社交登录、OAuth2.0、单点登录）
├── gulimall-cart -- 购物车服务
├── gulimall-coupon -- 优惠卷服务
├── gulimall-gateway -- 统一配置网关
├── gulimall-order -- 订单服务
├── gulimall-product -- 商品服务
├── gulimall-search -- 检索服务
├── gulimall-seckill -- 秒杀服务
├── gulimall-third-party -- 第三方服务
├── gulimall-ware -- 仓储服务
└── gulimall-member -- 会员服务
```

![image](https://user-images.githubusercontent.com/102777678/218044377-789f707a-798e-4415-8f2f-5f1a29739165.png)




## 四、项目涉及到的技术



**开发环境**

| 工具          | 版本号 | 下载                                                         |
| ------------- | ------ | ------------------------------------------------------------ |
| JDK           | 1.8    | [https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html](https://gitee.com/link?target=https%3A%2F%2Fwww.oracle.com%2Fjava%2Ftechnologies%2Fjavase%2Fjavase-jdk8-downloads.html) |
| Mysql         | 5.7    | [https://www.mysql.com](https://gitee.com/link?target=https%3A%2F%2Fwww.mysql.com) |
| Redis         | Redis  | [https://redis.io/download](https://gitee.com/link?target=https%3A%2F%2Fredis.io%2Fdownload) |
| Elasticsearch | 7.6.2  | [https://www.elastic.co/downloads](https://gitee.com/link?target=https%3A%2F%2Fwww.elastic.co%2Fdownloads) |
| Kibana        | 7.6.2  | [https://www.elastic.co/cn/kibana](https://gitee.com/link?target=https%3A%2F%2Fwww.elastic.co%2Fcn%2Fkibana) |
| RabbitMQ      | 3.8.5  | [http://www.rabbitmq.com/download.html](https://gitee.com/link?target=http%3A%2F%2Fwww.rabbitmq.com%2Fdownload.html) |
| Nginx         | 1.1.6  | [http://nginx.org/en/download.html](https://gitee.com/link?target=http%3A%2F%2Fnginx.org%2Fen%2Fdownload.html) |



**开发工具**

| 工具          | 说明                             | 官网                                                         |
| ------------- | -------------------------------- | ------------------------------------------------------------ |
| IDEA          | 开发平台                         | [https://www.jetbrains.com/idea/download](https://gitee.com/link?target=https%3A%2F%2Fwww.jetbrains.com%2Fidea%2Fdownload) |
| RedisDesktop  | redis客户端连接工具              | [https://redisdesktop.com/download](https://gitee.com/link?target=https%3A%2F%2Fredisdesktop.com%2Fdownload) |
| X-shell       | Linux远程连接工具                | [XSHELL - NetSarang Website](https://www.xshell.com/zh/xshell/) |
| X-FTP         | Linux远程传输工具                | [XFTP - NetSarang Website (xshell.com)](https://www.xshell.com/zh/xftp/) |
| SQL Yog       | 数据库连接工具                   | [SQLyog - Download (softonic.com)](https://sqlyog.en.softonic.com/) |
| PowerDesigner | 数据库设计工具                   | [http://powerdesigner.de](https://gitee.com/link?target=http%3A%2F%2Fpowerdesigner.de) |
| Postman       | API接口调试工具                  | [https://www.postman.com](https://gitee.com/link?target=https%3A%2F%2Fwww.postman.com) |
| Jmeter        | 性能压测工具                     | [https://jmeter.apache.org](https://gitee.com/link?target=https%3A%2F%2Fjmeter.apache.org) |
| Typora        | Markdown编辑器                   | [https://typora.io](https://gitee.com/link?target=https%3A%2F%2Ftypora.io) |
| 人人开源      | 快速搭建后台管理系统，代码生成器 | https://gitee.com/renrenio                                   |



**前端技术**

| 技术      | 说明       | 官网                                                         |
| --------- | ---------- | ------------------------------------------------------------ |
| Vue       | 前端框架   | [https://vuejs.org](https://gitee.com/link?target=https%3A%2F%2Fvuejs.org) |
| Element   | 前端UI框架 | [https://element.eleme.io](https://gitee.com/link?target=https%3A%2F%2Felement.eleme.io) |
| thymeleaf | 模板引擎   | [https://www.thymeleaf.org](https://gitee.com/link?target=https%3A%2F%2Fwww.thymeleaf.org) |
| node.js   | 服务端的js | [https://nodejs.org/en](https://gitee.com/link?target=https%3A%2F%2Fnodejs.org%2Fen) |



**后端技术**

| 技术               | 说明                      | 官网                                                         |
| ------------------ | ------------------------- | ------------------------------------------------------------ |
| SpringBoot         | 基础开发环境              | [https://spring.io/projects/spring-boot](https://gitee.com/link?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-boot) |
| SpringCloud        | 分布式组件                | [https://spring.io/projects/spring-cloud](https://gitee.com/link?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-cloud) |
| SpringCloudAlibaba | 分布式组件                | [https://spring.io/projects/spring-cloud-alibaba](https://gitee.com/link?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-cloud-alibaba) |
| MyBatis-Plus       | 数据库操作框架            | [https://mp.baomidou.com](https://gitee.com/link?target=https%3A%2F%2Fmp.baomidou.com) |
| Elasticsearch      | 搜索引擎                  | [https://github.com/elastic/elasticsearch](https://gitee.com/link?target=https%3A%2F%2Fgithub.com%2Felastic%2Felasticsearch) |
| RabbitMQ           | 消息队列                  | [https://www.rabbitmq.com](https://gitee.com/link?target=https%3A%2F%2Fwww.rabbitmq.com) |
| Springsession      | 解决分布式session共享问题 | [https://projects.spring.io/spring-session](https://gitee.com/link?target=https%3A%2F%2Fprojects.spring.io%2Fspring-session) |
| SpringCache        | 简化redis 缓存开发        | [Integration (spring.io)](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache) |
| Redisson           | 分布式锁                  | [https://github.com/redisson/redisson](https://gitee.com/link?target=https%3A%2F%2Fgithub.com%2Fredisson%2Fredisson) |
| Docker             | 应用容器引擎              | [https://www.docker.com](https://gitee.com/link?target=https%3A%2F%2Fwww.docker.com) |
| OSS                | 对象云存储                | [https://github.com/aliyun/aliyun-oss-java-sdk](https://gitee.com/link?target=https%3A%2F%2Fgithub.com%2Faliyun%2Faliyun-oss-java-sdk) |
| 云短信             | 阿里云短信功能            | [短信服务 (aliyun.com)](https://dysms.console.aliyun.com/overview) |



**SpringCloud Alibaba 组件**

| 组件     | 说明               | 文档                                                         |
| -------- | ------------------ | ------------------------------------------------------------ |
| Nacos    | 注册中心、配置中心 | [Nacos 快速开始](https://nacos.io/zh-cn/docs/quick-start.html) |
| Seata    | 分布式事务         | [Seata 是什么](http://seata.io/zh-cn/docs/overview/what-is-seata.html) |
| Sentinel | 熔断、限流、降级   | https://sentinelguard.io/zh-cn/docs/introduction.html        |



**SpringCloud 组件**

| 组件      | 说明           | 文档                                                         |
| --------- | -------------- | ------------------------------------------------------------ |
| OpenFeign | 各服务远程调用 | [Spring Cloud OpenFeign](https://docs.spring.io/spring-cloud-openfeign/docs/3.1.3/reference/html/) |
| Gateway   | 网关           | [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-starter) |

