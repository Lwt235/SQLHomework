# 部署指南

## 环境要求
- **JDK**: Java 17 或更高版本
- **Maven**: 3.6 或更高版本
- **MySQL**: 8.0 或更高版本
- **操作系统**: Linux, macOS 或 Windows

## 数据库准备

### 1. 创建数据库
```sql
CREATE DATABASE competition_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 导入数据库结构
```bash
mysql -u root -p competition_management < CollegeStudentCompetitionManagementSystem_mysql.sql
```

### 3. 创建初始角色（可选）
```sql
USE competition_management;

-- 插入角色
INSERT INTO Role (role_id, role_code, role_name, description, created_at, updated_at, is_deleted) 
VALUES 
(1, 'ROLE_ADMIN', '系统管理员', '拥有所有权限', NOW(), NOW(), 0),
(2, 'ROLE_ORGANIZER', '竞赛组织者', '可以创建和管理竞赛', NOW(), NOW(), 0),
(3, 'ROLE_JUDGE', '评委', '可以评审作品', NOW(), NOW(), 0),
(4, 'ROLE_STUDENT', '学生', '可以参加竞赛', NOW(), NOW(), 0);
```

## 应用配置

### 1. 修改数据库连接
编辑 `src/main/resources/application.properties`:

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/competition_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=你的用户名
spring.datasource.password=你的密码

# JWT密钥（生产环境请更换为强密钥）
jwt.secret=你的JWT密钥至少32个字符长
jwt.expiration=86400000

# 文件上传目录
file.upload-dir=./uploads
```

### 2. JWT密钥生成建议
生产环境请使用强密钥，可以使用以下命令生成：
```bash
# Linux/Mac
openssl rand -base64 64

# 或使用UUID
uuidgen
```

## 构建项目

### 方式1：使用Maven Wrapper（推荐）
```bash
# Linux/Mac
./mvnw clean package

# Windows
mvnw.cmd clean package
```

### 方式2：使用本地Maven
```bash
mvn clean package
```

构建成功后，会在 `target` 目录下生成 `competition-management-1.0.0.jar`

## 运行应用

### 开发模式
```bash
# 使用Maven运行
mvn spring-boot:run

# 或使用Java运行jar包
java -jar target/competition-management-1.0.0.jar
```

### 生产模式
```bash
# 后台运行
nohup java -jar target/competition-management-1.0.0.jar > app.log 2>&1 &

# 或使用systemd服务（Linux）
# 创建服务文件 /etc/systemd/system/competition.service
```

### 指定配置文件运行
```bash
# 使用外部配置文件
java -jar target/competition-management-1.0.0.jar --spring.config.location=file:/path/to/application.properties

# 指定端口
java -jar target/competition-management-1.0.0.jar --server.port=9090

# 指定环境
java -jar target/competition-management-1.0.0.jar --spring.profiles.active=prod
```

## 验证部署

### 1. 检查应用是否启动
```bash
# 查看日志
tail -f app.log

# 检查端口
netstat -tlnp | grep 8080
# 或
lsof -i :8080
```

### 2. 测试API
```bash
# 测试健康检查
curl http://localhost:8080/api/competitions/list

# 测试注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test001",
    "password": "password123",
    "realName": "测试用户",
    "email": "test@example.com"
  }'

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test001",
    "password": "password123"
  }'
```

## Docker 部署（可选）

### 创建 Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/competition-management-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
```

### 构建和运行
```bash
# 构建Docker镜像
docker build -t competition-management:1.0.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/competition_management \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=yourpassword \
  --name competition-api \
  competition-management:1.0.0
```

### Docker Compose（完整方案）
创建 `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: competition_management
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./CollegeStudentCompetitionManagementSystem_mysql.sql:/docker-entrypoint-initdb.d/init.sql
  
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/competition_management
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: rootpassword
    depends_on:
      - mysql

volumes:
  mysql-data:
```

运行：
```bash
docker-compose up -d
```

## 生产环境建议

### 1. 安全配置
- 更换默认的JWT密钥为强密钥
- 使用HTTPS（配置SSL证书）
- 配置防火墙规则
- 使用数据库连接池
- 定期备份数据库

### 2. 性能优化
```properties
# application-prod.properties

# 数据库连接池
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JPA优化
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# 日志级别
logging.level.root=WARN
logging.level.com.competition=INFO

# 启用压缩
server.compression.enabled=true
```

### 3. 监控和日志
- 集成Spring Boot Actuator进行健康检查
- 配置日志轮转（logback配置）
- 使用监控工具（如Prometheus + Grafana）

### 4. 反向代理（Nginx）
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否运行
- 验证用户名和密码是否正确
- 确认数据库名称是否正确
- 检查防火墙设置

### 2. 端口冲突
```bash
# 查看端口占用
lsof -i :8080

# 修改端口
java -jar app.jar --server.port=9090
```

### 3. 内存不足
```bash
# 增加JVM堆内存
java -Xmx1024m -Xms512m -jar app.jar
```

### 4. JWT Token失效
- 检查系统时间是否同步
- 验证JWT密钥是否一致
- 确认Token过期时间设置

## 维护命令

```bash
# 查看应用进程
ps aux | grep competition-management

# 停止应用
kill -15 <PID>

# 查看日志
tail -f app.log

# 备份数据库
mysqldump -u root -p competition_management > backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p competition_management < backup_20241120.sql
```

## 升级指南

1. 备份现有数据库
2. 停止旧版本应用
3. 部署新版本jar包
4. 执行数据库迁移脚本（如有）
5. 启动新版本应用
6. 验证功能正常

## 技术支持
如有问题，请查阅：
- [API文档](API.md)
- [README](README.md)
- GitHub Issues
