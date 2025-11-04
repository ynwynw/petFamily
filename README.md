## 本项目完整源码是收费的  接毕业设计和论文

### 作者微信：grapro666 QQ：3642795578 (支持部署调试、支持代做毕设)

### 接javaweb、python、小程序、H5、APP、各种管理系统、单片机、嵌入式等开发

### 选题+开题报告+任务书+程序定制+安装调试+论文+答辩ppt

**博客地址：
[https://blog.csdn.net/2303_76227485/article/details/154385262](https://blog.csdn.net/2303_76227485/article/details/154385262)**

**视频演示：
[https://www.bilibili.com/video/BV1cH1KBaEEd/](https://www.bilibili.com/video/BV1cH1KBaEEd/)**

**毕业设计所有选题地址：
[https://github.com/ynwynw/allProject](https://github.com/ynwynw/allProject)**

## 基于Java+Springboot+vue+AI智能的宠物之家系统(源代码+数据库+2万字论文+任务书)244
## 一、系统介绍
本项目前后端分离，分为用户、管理员两种角色。
### 1、用户：
- 注册、登录、消息通知、宠物领养申请、商品购买、沙箱支付、AI智能推荐宠物、宠物AI聊天功能、宠物寄养服务、宠物医疗服务、
- 宠物训练服务、宠物美容服务、流浪宠物上报、个人信息管理、密码修改
### 2、管理员：
- 宠物总数、用户总数、领养总数、订单总数、寄养总数统计，领养趋势曲线图、宠物状态分布环状图、宠物品种分布统计、通知公告
- 宠物管理、领养管理（审核、状态追踪）、宠物登记、宠物房间管理、品种管理、宠物健康管理
- 商品管理、订单管理、订单发货
- 通知管理、用户管理、轮播图管理、推送管理
- 宠物寄养服务、宠物医疗服务、宠物训练服务、美容服务、训练项目管理、寄养上报

### 3、亮点
(1)、使用支付宝沙箱模拟真实支付

(2)、使用了讯飞星火大模型实现ai智能对话和推荐

(3)、注册使用smtp邮箱发送验证码

## 二、所用技术
后端技术栈：
- Springboot
- mybatisPlus
- Mysql
- Maven
- smtp邮箱
- 星火AI模型
- 支付宝沙箱支付

前端技术栈：
- Vue
- Vue-router
- axios
- elementui
- echarts

## 三、环境介绍
基础环境 :IDEA/eclipse, JDK1.8, Mysql5.7及以上, Maven3.6, node14, navicat、支付宝沙箱账号、ngrok软件和账号、qq邮箱的smtp授权秘钥、讯飞星火ai参数

所有项目以及源代码本人均调试运行无问题 可支持远程调试运行

## 四、页面截图
### 文档：
![](./picture/picture000.png)
![](./picture/picture00.png)
![](./picture/picture0.png)
### 1、用户：
![contents](./picture/picture1.png)
![contents](./picture/picture2.png)
![contents](./picture/picture3.png)
![contents](./picture/picture4.png)
![contents](./picture/picture5.png)
![contents](./picture/picture6.png)
![contents](./picture/picture7.png)
![contents](./picture/picture8.png)
![contents](./picture/picture9.png)
![contents](./picture/picture10.png)
![contents](./picture/picture11.png)
![contents](./picture/picture12.png)
![contents](./picture/picture13.png)
![contents](./picture/picture14.png)
![contents](./picture/picture15.png)
![contents](./picture/picture16.png)
![contents](./picture/picture17.png)
![contents](./picture/picture18.png)
![contents](./picture/picture19.png)
![contents](./picture/picture20.png)
![contents](./picture/picture21.png)
![contents](./picture/picture22.png)
![contents](./picture/picture23.png)
![contents](./picture/picture24.png)
![contents](./picture/picture25.png)
![contents](./picture/picture26.png)
![contents](./picture/picture27.png)
![contents](./picture/picture28.png)
![contents](./picture/picture29.png)
![contents](./picture/picture30.png)
![contents](./picture/picture31.png)
![contents](./picture/picture32.png)
![contents](./picture/picture33.png)
### 2、管理员：
![contents](./picture/picture34.png)
![contents](./picture/picture35.png)
![contents](./picture/picture36.png)
![contents](./picture/picture37.png)
![contents](./picture/picture38.png)
![contents](./picture/picture39.png)
![contents](./picture/picture40.png)
![contents](./picture/picture41.png)
![contents](./picture/picture42.png)
![contents](./picture/picture43.png)
![contents](./picture/picture44.png)
![contents](./picture/picture45.png)
![contents](./picture/picture46.png)
![contents](./picture/picture47.png)
![contents](./picture/picture48.png)
![contents](./picture/picture49.png)
![contents](./picture/picture50.png)
![contents](./picture/picture51.png)
![contents](./picture/picture52.png)
![contents](./picture/picture53.png)
![contents](./picture/picture54.png)
![contents](./picture/picture55.png)
![contents](./picture/picture56.png)
## 五、浏览地址
访问地址：http://localhost:8080

- 用户账号密码：user/123456
- 管理员账户密码：admin/123456


## 六、部署教程
1. 使用Navicat或者其它工具，在mysql中创建对应名称的数据库，并执行项目的sql文件

2. 使用IDEA/Eclipse导入petBack项目，若为maven项目请选择maven，等待依赖下载完成

3. 修改application.yml里面的讯飞星火大模型配置,application.properties里面的数据库配置，smtp邮箱配置，支付宝沙箱配置(还要用ngrok先将内网穿透配置好)，
src/main/java/com/example/petback/PetBackApplication.java启动后端项目

4. vscode或idea打开petFront后台项目

5. 在编译器中打开terminal，执行npm install 依赖下载完成后执行 npm run serve,执行成功后会显示访问地址

## 七、ngrok配置内网穿透
1、打开ngrok

2、执行命令 ngrok http http//ip:端口

3、将生成的地址替换代码中的回调地址
