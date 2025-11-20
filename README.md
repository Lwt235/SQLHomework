# 竞赛管理系统 - Spring Boot 后端

## 项目简介
基于 Spring Boot 3.2 开发的竞赛管理系统后端，实现用户管理、竞赛管理、报名管理、作品提交、评审管理、成绩奖项和消息通知七大核心功能模块。

## 技术栈
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security 6.x
- MySQL 8.0+
- JWT Authentication
- Lombok
- MapStruct

## 功能模块
1. 用户管理：注册、认证、密码找回、管理员审核、权限分配
2. 竞赛管理：竞赛发布、规则设置、状态管理、信息查询
3. 报名管理：个人/团队报名、报名审核、报名统计
4. 作品提交：作品提交、管理、截止时间提醒
5. 评审管理：评审分配、在线评审、成绩核算、进度跟踪
6. 成绩奖项：生成获奖名单、公示结果、成绩查询、证书生成
7. 消息通知：报名审核、作品提醒、获奖通知

## 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 数据库初始化
1. 创建数据库：`CREATE DATABASE competition_db;`
2. 执行数据库脚本（参考 sql 目录）

### 配置
修改 `src/main/resources/application.yml` 中的数据库连接信息

### 运行
```bash
mvn spring-boot:run
```

访问: http://localhost:8080

## API 文档
启动后访问: http://localhost:8080/swagger-ui.html

## 项目结构
```
src/main/java/com/competition/
├── entity/          # 实体类
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
├── controller/      # REST API
├── dto/             # 数据传输对象
├── security/        # 安全配置
├── exception/       # 异常处理
├── enums/           # 枚举类
└── util/            # 工具类
```
