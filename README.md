# 竞赛管理系统 - Spring Boot 后端 + Vue 前端

## 项目简介
基于 Spring Boot 3.2 + Vue 3 开发的竞赛管理系统，实现用户管理、竞赛管理、报名管理、作品提交、评审管理、成绩奖项和消息通知七大核心功能模块。

**重要更新**: 用户注册后需要管理员审核激活才能登录使用系统。

## 功能模块
1. **用户管理**：注册（需管理员审核激活）、认证、密码找回、权限分配
2. **竞赛管理**：竞赛发布、规则设置、状态管理、信息查询
3. **报名管理**：个人/团队报名、报名审核、报名统计
4. **作品提交**：作品提交、管理、截止时间提醒
5. **评审管理**：评审分配、在线评审、成绩核算、进度跟踪
6. **成绩奖项**：生成获奖名单、公示结果、成绩查询、证书生成
7. **消息通知**：报名审核、作品提醒、获奖通知

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **构建工具**: Maven
- **JDK**: Java 17

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **UI 框架**: Element Plus
- **HTTP 客户端**: Axios

## 快速开始

### 前置要求
- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ 和 npm

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

### 后端启动

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

4. 后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 前端应用将在 `http://localhost:5173` 启动

5. 生产环境构建：
```bash
npm run build
```

### API 文档
详细的 API 文档请查看 [API.md](API.md)

### 主要 API 端点
- **认证**: `/api/auth/login`, `/api/auth/register`
- **用户管理**: `/api/users` (管理员审核激活用户)
- **竞赛**: `/api/competitions`
- **报名**: `/api/registrations`
- **作品**: `/api/submissions`
- **评审**: `/api/judge/assignments`
- **奖项**: `/api/awards`

## 用户注册与审核流程

### 学生注册
1. 访问前端注册页面填写信息
2. 提交注册后，账号状态为 **"未激活"(inactive)**
3. 系统提示：需要等待管理员审核
4. 此时**无法登录**系统

### 管理员审核
1. 管理员登录系统
2. 进入 **"管理后台 > 用户管理"**
3. 点击 **"待审核用户"** 查看所有未激活用户
4. 查看用户信息后点击 **"激活"** 按钮
5. 用户状态变更为 **"激活"(active)**
6. 用户现在可以登录使用系统

### 用户状态说明
- **inactive**: 新注册用户，需要管理员审核
- **active**: 已激活，可以正常使用
- **suspended**: 已暂停，管理员可暂停违规用户

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

