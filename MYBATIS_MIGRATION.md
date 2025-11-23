# Hibernate to MyBatis Migration Guide

## Current Status

This document describes the migration from Spring Data JPA (Hibernate) to MyBatis for the Competition Management System.

## Completed Steps

### 1. Dependencies Updated (pom.xml)
- ✅ Removed `spring-boot-starter-data-jpa`
- ✅ Added `mybatis-spring-boot-starter` version 3.0.3

### 2. Configuration Updated (application.properties)
- ✅ Removed JPA/Hibernate configuration
- ✅ Added MyBatis configuration:
  ```properties
  mybatis.mapper-locations=classpath:mapper/*.xml
  mybatis.type-aliases-package=com.competition.entity
  mybatis.configuration.map-underscore-to-camel-case=true
  mybatis.configuration.log-impl=org.apache.ibatis.logging.slf4j.Slf4jImpl
  ```

### 3. Entity Classes Cleaned
- ✅ Removed all JPA annotations (@Entity, @Table, @Id, @Column, etc.)
- ✅ Removed @PrePersist and @PreUpdate lifecycle methods
- ✅ Kept Lombok @Data annotations
- ✅ Entities are now plain POJOs

### 4. MyBatis Mappers Created
All 12 mapper interfaces have been created in `src/main/java/com/competition/mapper/`:
- ✅ UserMapper
- ✅ RoleMapper
- ✅ UserRoleMapper
- ✅ CompetitionMapper
- ✅ TeamMapper
- ✅ TeamMemberMapper
- ✅ RegistrationMapper
- ✅ SubmissionMapper
- ✅ AwardMapper
- ✅ AwardResultMapper
- ✅ JudgeAssignmentMapper
- ✅ NotificationMapper

Each mapper includes basic CRUD operations using MyBatis annotations (@Select, @Insert, @Update, @Delete).

### 5. Main Application Updated
- ✅ Added @MapperScan("com.competition.mapper") to enable MyBatis mappers

### 6. Repository Package Removed
- ✅ Deleted all 12 repository interfaces that extended JpaRepository

### 7. Services Updated (3 of 12)
- ✅ UserService - fully migrated to use mappers
- ✅ AuthService - fully migrated to use mappers
- ✅ CustomUserDetailsService - fully migrated to use mappers

## Remaining Work

### 1. Update Remaining Service Classes

The following 9 service files still reference JPA repositories and need to be updated:

1. **AwardService.java** - Uses AwardMapper, AwardResultMapper
2. **CompetitionService.java** - Uses CompetitionMapper
3. **CompetitionStatusScheduler.java** - Uses CompetitionMapper
4. **JudgeService.java** - Uses JudgeAssignmentMapper, SubmissionMapper, UserMapper, UserRoleMapper, RoleMapper
5. **NotificationService.java** - Uses NotificationMapper
6. **RegistrationService.java** - Uses RegistrationMapper, CompetitionMapper, UserMapper, TeamMemberMapper, UserRoleMapper, RoleMapper
7. **SoftDeleteManagementService.java** - Uses multiple mappers
8. **SubmissionService.java** - Uses SubmissionMapper, RegistrationMapper, UserMapper, UserRoleMapper, RoleMapper
9. **TeamService.java** - Uses TeamMapper, TeamMemberMapper, UserMapper, UserRoleMapper, RoleMapper

### 2. Key Migration Patterns

When updating services, follow these patterns:

#### Import Changes
```java
// OLD
import com.competition.repository.UserRepository;

// NEW
import com.competition.mapper.UserMapper;
```

#### Field Declaration Changes
```java
// OLD
@Autowired
private UserRepository userRepository;

// NEW
@Autowired
private UserMapper userMapper;
```

#### Method Call Changes

**findById:**
```java
// OLD (JPA returns Optional)
User user = userRepository.findById(userId)
    .orElseThrow(() -> new RuntimeException("User not found"));

// NEW (MyBatis returns null)
User user = userMapper.findById(userId);
if (user == null) {
    throw new RuntimeException("User not found");
}
```

**save (for inserts):**
```java
// OLD
User user = new User();
// ... set properties
userRepository.save(user);

// NEW
User user = new User();
// ... set properties
user.setCreatedAt(LocalDateTime.now());
user.setUpdatedAt(LocalDateTime.now());
userMapper.insert(user); // ID is auto-generated
```

**save (for updates):**
```java
// OLD
user.setUsername("newname");
userRepository.save(user);

// NEW  
user.setUsername("newname");
userMapper.update(user);
```

**existsByXXX:**
```java
// OLD
boolean exists = userRepository.existsByUsername(username);

// NEW (returns boolean via COUNT)
boolean exists = userMapper.existsByUsername(username);
```

**findAllById:**
```java
// OLD
List<Role> roles = roleRepository.findAllById(roleIds);

// NEW (need to iterate)
List<Role> roles = roleIds.stream()
    .map(roleId -> roleMapper.findById(roleId))
    .filter(role -> role != null)
    .collect(Collectors.toList());
```

**deleteAll:**
```java
// OLD
userRoleRepository.deleteAll(existingRoles);

// NEW (use batch delete or iterate)
userRoleMapper.deleteByUserId(userId);
// OR iterate for individual deletes
```

### 3. Add XML Mapper Files for Complex Operations

Create XML mapper files in `src/main/resources/mapper/` for:
- Complex SQL queries with joins
- Stored procedures
- Triggers
- Batch operations
- Dynamic SQL

Example XML mapper structure:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.competition.mapper.UserMapper">
    <!-- Complex queries, stored procedures, etc. -->
    
    <select id="searchUsersWithRoles" resultType="UserDTO">
        SELECT u.*, r.role_name 
        FROM User u
        LEFT JOIN UserRole ur ON u.user_id = ur.user_id
        LEFT JOIN Role r ON ur.role_id = r.role_id
        WHERE u.is_deleted = FALSE
    </select>
    
    <!-- Stored procedure example -->
    <select id="callCalculateAwards" statementType="CALLABLE">
        {CALL calculate_competition_awards(#{competitionId})}
    </select>
</mapper>
```

### 4. Create Database Scripts

Create SQL scripts for:
- Stored procedures (e.g., for calculating awards, updating statuses)
- Triggers (e.g., for automatic timestamp updates, audit logging)
- Views (if needed for complex queries)

Place these in a new directory: `src/main/resources/db/`

### 5. Add Missing Mapper Methods

Some service files may need additional mapper methods that aren't currently defined. Add them to the respective mapper interfaces as needed.

For example, if a service needs `findBySubmissionStatus()`:
```java
@Select("SELECT * FROM Submission WHERE submission_status = #{status} AND is_deleted = FALSE")
@ResultMap("submissionResultMap")
List<Submission> findBySubmissionStatus(String status);
```

### 6. Handle Transactional Behavior

MyBatis requires explicit transaction management. Services using `@Transactional` should continue to work, but verify that:
- Multiple insert/update/delete operations within a transaction are properly rolled back on error
- Isolation levels are appropriate

### 7. Testing

After completing the migration:
1. Test all CRUD operations for each entity
2. Test complex queries and joins
3. Test stored procedures and triggers
4. Verify soft delete functionality
5. Test user authentication and authorization
6. Run integration tests

## Benefits of This Migration

1. **Explicit SQL Control**: You can now write explicit SQL including stored procedures and triggers
2. **Better Performance Tuning**: Direct control over SQL queries
3. **Database Features**: Can use database-specific features and optimizations
4. **Lighter Weight**: MyBatis has less overhead than Hibernate

## Notes

- The original database schema remains unchanged
- Soft delete pattern is maintained
- All field naming follows snake_case in database, camelCase in Java (handled by MyBatis configuration)
- Auto-increment primary keys are handled by `@Options(useGeneratedKeys = true)`
