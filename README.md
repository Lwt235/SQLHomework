# 竞赛管理系统 - Spring Boot 后端

## 项目简介
基于 Spring Boot 3.2 开发的竞赛管理系统后端，实现用户管理、竞赛管理、报名管理、作品提交、评审管理、成绩奖项和消息通知七大核心功能模块。

## 功能模块
1. **用户管理**：注册、认证、密码找回、管理员审核、权限分配
2. **竞赛管理**：竞赛发布、规则设置、状态管理、信息查询
3. **报名管理**：个人/团队报名、报名审核、报名统计
4. **作品提交**：作品提交、管理、截止时间提醒
5. **评审管理**：评审分配、在线评审、成绩核算、进度跟踪
6. **成绩奖项**：生成获奖名单、公示结果、成绩查询、证书生成
7. **消息通知**：报名审核、作品提醒、获奖通知

## 技术栈
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **构建工具**: Maven
- **JDK**: Java 17

## 快速开始

### 前置要求
- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+

### 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE competition_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本：
```bash
mysql -u root -p competition_management < CollegeStudentCompetitionManagementSystem_mysql.sql
```

3. 修改 `src/main/resources/application.properties` 中的数据库配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/competition_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 构建和运行

1. 克隆项目：
```bash
git clone https://github.com/Lwt235/SQLHomework.git
cd SQLHomework
```

2. 编译项目：
```bash
mvn clean compile
```

3. 运行项目：
```bash
mvn spring-boot:run
```

4. 应用将在 `http://localhost:8080` 启动

### API 文档
详细的 API 文档请查看 [API.md](API.md)

### 主要 API 端点
- **认证**: `/api/auth/login`, `/api/auth/register`
- **竞赛**: `/api/competitions`
- **报名**: `/api/registrations`
- **作品**: `/api/submissions`
- **评审**: `/api/judge/assignments`
- **奖项**: `/api/awards`

## 项目结构
```
src/
├── main/
│   ├── java/com/competition/
│   │   ├── CompetitionManagementApplication.java  # 主应用类
│   │   ├── config/                                # 配置类
│   │   │   └── SecurityConfig.java               # 安全配置
│   │   ├── controller/                           # 控制器层
│   │   │   ├── AuthController.java
│   │   │   ├── CompetitionController.java
│   │   │   ├── RegistrationController.java
│   │   │   ├── SubmissionController.java
│   │   │   ├── JudgeController.java
│   │   │   └── AwardController.java
│   │   ├── dto/                                  # 数据传输对象
│   │   │   ├── ApiResponse.java
│   │   │   ├── JwtResponse.java
│   │   │   ├── LoginRequest.java
│   │   │   └── RegisterRequest.java
│   │   ├── entity/                               # 实体类
│   │   │   ├── User.java
│   │   │   ├── Competition.java
│   │   │   ├── Registration.java
│   │   │   ├── Submission.java
│   │   │   ├── Award.java
│   │   │   └── ...
│   │   ├── repository/                           # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── CompetitionRepository.java
│   │   │   └── ...
│   │   ├── security/                             # 安全相关
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailsService.java
│   │   └── service/                              # 业务逻辑层
│   │       ├── AuthService.java
│   │       ├── CompetitionService.java
│   │       ├── RegistrationService.java
│   │       ├── SubmissionService.java
│   │       ├── JudgeService.java
│   │       └── AwardService.java
│   └── resources/
│       └── application.properties                # 应用配置
└── test/                                         # 测试代码
```

## 数据库说明
数据库脚本文件：`CollegeStudentCompetitionManagementSystem_mysql.sql`

主要数据表：
- `User` - 用户表
- `Role` - 角色表
- `UserRole` - 用户角色关联表
- `Competition` - 竞赛表
- `Registration` - 报名表
- `Team` - 团队表
- `Submission` - 作品提交表
- `JudgeAssignment` - 评审分配表
- `Award` - 奖项表
- `AwardResult` - 获奖结果表
- `File` - 文件表
- `Rule` - 规则表

## 开发说明
- 使用 JWT 进行身份认证，Token 有效期为 24 小时
- 所有 API 支持 CORS 跨域访问
- 采用软删除策略，删除操作不会真正删除数据
- 使用 BCrypt 对用户密码进行加密存储

## License
MIT License

