# Hibernate 到 MyBatis 迁移 - 当前进度

## 概述

本项目正在从 Spring Data JPA (Hibernate) 迁移到 MyBatis，以支持显式编写触发器、存储过程等数据库操作。

## 已完成的工作 ✓

### 1. 基础设施迁移
- ✅ 更新 pom.xml：移除 Spring Data JPA，添加 MyBatis Spring Boot Starter 3.0.3
- ✅ 更新 application.properties：配置 MyBatis
- ✅ 添加 @MapperScan 注解到主应用类

### 2. 实体类清理
- ✅ 移除所有 JPA 注解（@Entity, @Table, @Id, @Column 等）
- ✅ 移除 @PrePersist 和 @PreUpdate 生命周期方法
- ✅ 保留 Lombok @Data 注解
- ✅ 所有实体类现在都是纯 POJO

### 3. MyBatis Mapper 创建
在 `src/main/java/com/competition/mapper/` 目录下创建了 12 个 Mapper 接口：
- UserMapper
- RoleMapper
- UserRoleMapper
- CompetitionMapper
- TeamMapper
- TeamMemberMapper
- RegistrationMapper
- SubmissionMapper
- AwardMapper
- AwardResultMapper
- JudgeAssignmentMapper
- NotificationMapper

每个 Mapper 都包含基本的 CRUD 操作，使用 MyBatis 注解（@Select, @Insert, @Update, @Delete）。

### 4. Repository 删除
- ✅ 删除了所有 12 个继承 JpaRepository 的 Repository 接口

### 5. Service 更新（3/12 已完成）
- ✅ UserService - 已完全迁移
- ✅ AuthService - 已完全迁移
- ✅ CustomUserDetailsService - 已完全迁移

### 6. 文档和示例
创建了三个重要文件：

#### MYBATIS_MIGRATION.md
完整的迁移指南，包含：
- 详细的迁移模式和示例
- JPA 到 MyBatis 的转换规则
- 剩余需要更新的服务列表

#### src/main/resources/db/stored_procedures_and_triggers.sql
SQL 示例文件，包含：
- 存储过程（奖项计算、状态更新）
- 触发器（自动时间戳、通知发送、数据验证）
- 视图（复杂查询）

#### src/main/resources/mapper/CompetitionMapper.xml
XML Mapper 示例，展示：
- 复杂查询（JOIN）
- 调用存储过程
- 动态 SQL
- 批量操作

## 待完成的工作 ⚠️

### 剩余 9 个 Service 需要更新

以下 Service 文件仍在使用 JPA Repository，需要手动更新为使用 MyBatis Mapper：

1. **AwardService.java**
2. **CompetitionService.java**
3. **CompetitionStatusScheduler.java**
4. **JudgeService.java**
5. **NotificationService.java**
6. **RegistrationService.java**
7. **SoftDeleteManagementService.java**
8. **SubmissionService.java**
9. **TeamService.java**

### 更新步骤

对于每个 Service 文件：

1. **更新 import 语句**
   ```java
   // 将
   import com.competition.repository.UserRepository;
   // 改为
   import com.competition.mapper.UserMapper;
   ```

2. **更新字段声明**
   ```java
   // 将
   @Autowired
   private UserRepository userRepository;
   // 改为
   @Autowired
   private UserMapper userMapper;
   ```

3. **更新方法调用**
   
   **findById:**
   ```java
   // JPA (返回 Optional)
   User user = userRepository.findById(userId)
       .orElseThrow(() -> new RuntimeException("User not found"));
   
   // MyBatis (返回 null)
   User user = userMapper.findById(userId);
   if (user == null) {
       throw new RuntimeException("User not found");
   }
   ```

   **save (插入):**
   ```java
   // JPA
   userRepository.save(user);
   
   // MyBatis
   user.setCreatedAt(LocalDateTime.now());
   user.setUpdatedAt(LocalDateTime.now());
   userMapper.insert(user);
   ```

   **save (更新):**
   ```java
   // JPA
   user.setUsername("newname");
   userRepository.save(user);
   
   // MyBatis
   user.setUsername("newname");
   userMapper.update(user);
   ```

   **findAllById:**
   ```java
   // JPA
   List<Role> roles = roleRepository.findAllById(roleIds);
   
   // MyBatis
   List<Role> roles = roleIds.stream()
       .map(roleId -> roleMapper.findById(roleId))
       .filter(role -> role != null)
       .collect(Collectors.toList());
   ```

详细的迁移模式请参考 `MYBATIS_MIGRATION.md` 文件。

### 添加缺失的 Mapper 方法

某些 Service 可能需要 Mapper 中不存在的方法。需要时请在相应的 Mapper 接口中添加。

例如，如果需要 `findBySubmissionStatus()`:
```java
@Select("SELECT * FROM Submission WHERE submission_status = #{status} AND is_deleted = FALSE")
@ResultMap("submissionResultMap")
List<Submission> findBySubmissionStatus(String status);
```

### 实施存储过程和触发器

1. 在 MySQL 数据库中执行 `src/main/resources/db/stored_procedures_and_triggers.sql`
2. 根据需要调整存储过程的逻辑
3. 在 Service 或 Mapper 中调用存储过程（参考 CompetitionMapper.xml 中的示例）

### 测试

完成所有 Service 更新后：
1. 运行单元测试
2. 测试所有 CRUD 操作
3. 测试复杂查询和 JOIN
4. 验证存储过程和触发器
5. 测试软删除功能
6. 测试用户认证和授权

## 如何继续

1. **阅读 MYBATIS_MIGRATION.md**：了解详细的迁移模式和示例
2. **逐个更新 Service**：按照上述步骤更新剩余的 9 个 Service 文件
3. **添加必要的 Mapper 方法**：如果 Service 需要的方法在 Mapper 中不存在，添加它们
4. **执行 SQL 脚本**：在数据库中创建存储过程和触发器
5. **测试**：确保所有功能正常工作

## 注意事项

- 原始数据库架构保持不变
- 保持软删除模式
- 自动递增主键由 `@Options(useGeneratedKeys = true)` 处理
- 字段命名：数据库使用 snake_case，Java 使用 camelCase（MyBatis 配置自动转换）
- 时间戳不再由 JPA 生命周期方法自动设置，需要在 insert/update 时手动设置或依赖触发器

## 支持

如有问题，请参考：
- MYBATIS_MIGRATION.md - 完整的迁移指南
- CompetitionMapper.xml - XML Mapper 示例
- stored_procedures_and_triggers.sql - 存储过程和触发器示例
- 已完成的 Service（UserService, AuthService）作为参考
