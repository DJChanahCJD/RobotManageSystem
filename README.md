

# RobotManageSystem - 工业机器人管理系统

## 项目简介
本项目是一个基于 iDME SDK 开发的工业管理实验项目。
>作为**最小可运行demo**，鉴权和路由都比较简陋，且前端仪表盘数据为mock数据，请谨慎使用，仅供交流学习。
>
>具体实验文档请参考`docs`文件夹（含测试数据集）

## 技术栈

### 前端
- [Ant Design Vue Pro](https://pro.antdv.com/docs/getting-started)

### 后端
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Huawei iDME SDK](https://codelabs.developer.huaweicloud.com/codelabs/samples/d26d2842cd9b4ece9ddf4cacc14f38e9)

## 项目结构
```
robotManageSystem/
├── backend/                # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── robotManageSystem/
│   │   │   │       ├── config/         # 配置类
│   │   │   │       ├── controller/     # 控制器
│   │   │   │       ├── dto/            # 数据传输对象
│   │   │   │       ├── interceptor/    # 拦截器
│   │   │   │       └── utils/          # 工具类
│   │   │   └── resources/              # 配置文件
│   └── pom.xml                         # Maven 配置
│
└── frontend/              # 前端项目
    ├── src/
    │   ├── api/          # API 接口
    │   ├── components/   # 公共组件
    │   ├── config/       # 配置文件
    │   ├── store/        # Vuex 状态管理
    │   ├── utils/        # 工具函数
    │   └── views/        # 页面组件
    └── package.json      # 依赖配置
```

## 主要功能
- 用户认证与授权
- 产品管理
- 蓝图管理
- 零件管理
- 订单管理
- 用户管理

## 运行方法

> Java版本：17
>
> OpenJDK: 17

### 后端启动

1. 完成 iDME 相关参数（可全局搜索“替换成你的”）
2. 运行 Spring Boot 应用：
```bash
cd backend
mvn spring-boot:run
// 或者直接在IDEA运行RobotManageSystemApplication.java
```

### 前端启动
1. 安装依赖：
```bash
cd frontend
yarn install
```

2. 开发环境运行：
```bash
yarn serve
```

## API 文档
- 认证相关 API：`/api/auth/*`
- 用户管理 API：`/api/user/*`
- 产品管理 API：`/api/product/*`
- 蓝图管理 API：`/api/blueprint/*`
- 部件件管理 API：`/api/part/*`

## 注意事项
1. 运行前必须在 [iDME 控制台](https://console.huaweicloud.com/dme) 完成应用发布并配置好相关参数
2. 开发注意事项:
   - 关注 jar 包中的 **dto、delegator、service** 等核心组件
   - 对于已有 ID 的实体进行操作时，**通常只需传入 ID**，iDME 会自动完成数据映射
3. 默认端口配置:
   - 前端服务: 8000
   - 后端服务: 8080
  
## 预览图
![image](https://github.com/user-attachments/assets/7f17757e-6664-4e42-87ca-8fec04cf75f3)

