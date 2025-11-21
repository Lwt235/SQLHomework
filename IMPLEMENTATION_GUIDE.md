# 功能实现说明文档

本次更新实现了问题列表中的全部6个需求，具体如下：

**重要说明：** 在User表中，`username` 字段即为用户的昵称，不需要额外添加 `nickname` 字段。

## 已实现功能清单

### 1. 个人中心添加修改个人信息功能

**前端实现：**
- 在 `UserProfile.vue` 页面添加了"修改个人信息"按钮
- 新增编辑对话框，支持修改以下信息：
  - 真实姓名
  - 邮箱
  - 手机号
  - 学号
  - 学校
  - 院系
- **注意：** 用户名(昵称)是登录凭证，不可修改

**后端实现：**
- 新增API端点：`PUT /api/users/{id}/profile`
- 在UserService中实现 `updateUserProfile()` 方法
- 支持邮箱唯一性校验

### 2. 团队卡片支持显示其他职位标注

**实现说明：**
- 原系统只支持显示"队长"标签
- 现已支持显示三种角色：
  - 🟡 **队长** (leader) - 黄色标签
  - 🟢 **副队长** (vice-leader) - 绿色标签
  - ⚪ **成员** (member) - 灰色标签

**修改文件：**
- `Teams.vue` - 更新团队卡片头部显示逻辑

### 3. 团队详情显示成员昵称并标注"我"

**前端实现：**
- 团队成员列表显示"用户名(昵称)"列
- 用户名即为昵称
- 对应"我"的那一行：
  - 用户名文本显示为蓝色加粗
  - 在用户名后添加蓝色"我"标签

**后端实现：**
- 新增DTO：`TeamMemberDetailDTO`，包含用户详细信息
- 新增API端点：`GET /api/teams/{id}/members/details`
- 实现 `getTeamMembersWithDetails()` 方法，返回带有用户名、真实姓名等信息的成员列表
- 标记当前登录用户的 `isCurrentUser` 字段

### 4. 添加队长移交功能

**功能描述：**
- 队长可以将队长身份转移给团队中的其他成员
- 转移后原队长变为普通成员，新队长获得管理权限

**实现细节：**
- 新增API端点：`POST /api/teams/{teamId}/transfer-captain`
- 在TeamService中实现 `transferCaptain()` 方法
- 前端在团队详情的成员列表中，每个非队长成员旁添加"转让队长"按钮
- 转让前弹出确认对话框，提示用户转让后的权限变更

### 5. 创建团队时通过昵称搜索成员

**功能描述：**
- 添加成员时支持通过用户名(昵称)搜索用户
- 搜索结果显示用户的用户名和ID
- 实现了500ms防抖，避免频繁请求

**实现细节：**
- 新增API端点：`GET /api/teams/search-users?query={关键词}`
- 在UserRepository中添加数据库查询方法 `searchByNicknameOrUsername()`
- 前端搜索框支持实时搜索，并在下拉列表中显示结果
- 可以为新成员分配"成员"或"副队长"角色

### 6. 报名时显示我作为队长的团队

**功能描述：**
- 选择团队参赛时，展示两种选项：
  1. 使用已有团队（显示我作为队长的所有团队）
  2. 创建新团队（输入新团队名称）

**实现细节：**
- 新增API端点：`GET /api/teams/my-captain-teams`
- 在TeamService中实现 `getTeamsByUserIdAndRole()` 方法，筛选指定角色的团队
- 前端在ApplyCompetition.vue中：
  - 选择团队参赛时自动加载队长团队列表
  - 如果有队长团队，默认选择"使用已有团队"
  - 下拉列表显示团队名称和简介
  - 如果没有队长团队，提示用户选择"创建新团队"

## 数据库变更

**无需数据库变更**。User表中的 `username` 字段即为用户昵称，不需要添加额外的 `nickname` 字段。

## API端点清单

### 新增端点：

1. **PUT** `/api/users/{id}/profile` - 更新用户个人信息
2. **GET** `/api/teams/{id}/members/details` - 获取团队成员详细信息
3. **POST** `/api/teams/{teamId}/transfer-captain` - 转让队长
4. **GET** `/api/teams/search-users?query={关键词}` - 搜索用户
5. **GET** `/api/teams/my-captain-teams` - 获取我作为队长的团队列表

## 性能优化

在代码审查后进行了以下优化：

1. **解决N+1查询问题**：
   - `getTeamMembersWithDetails()` 方法改为批量获取用户信息
   - 从逐个查询改为一次性查询所有用户，使用Map缓存

2. **优化用户搜索**：
   - 使用数据库LIKE查询搜索用户名(昵称)
   - 使用JPA的@Query注解实现高效的数据库级搜索

3. **添加搜索防抖**：
   - 前端搜索输入添加500ms防抖延迟
   - 避免每次输入都触发API请求

## 测试建议

### 1. 个人信息修改测试
- 登录系统后进入个人中心
- 查看用户名(昵称)是否正确显示（不可编辑）
- 点击"修改个人信息"按钮
- 修改真实姓名、邮箱等信息并保存
- 验证信息是否正确更新

### 2. 团队角色显示测试
- 创建团队并添加成员，分配不同角色
- 在"我的团队"页面查看团队卡片
- 验证不同角色的标签是否正确显示

### 3. 团队成员昵称显示测试
- 查看团队详情
- 验证用户名(昵称)列是否正确显示
- 验证当前用户行是否高亮并显示"我"标签

### 4. 队长移交测试
- 作为队长进入团队详情
- 点击某个成员旁的"转让队长"按钮
- 确认转让
- 验证角色是否正确变更

### 5. 昵称搜索测试
- 在团队详情中点击"添加成员"
- 在搜索框中输入用户名(昵称)
- 验证搜索结果是否正确显示
- 选择用户并添加

### 6. 报名团队选择测试
- 先创建一个团队（作为队长）
- 进入竞赛报名页面
- 选择"团队参赛"
- 验证是否显示"使用已有团队"和"创建新团队"选项
- 在已有团队中验证是否显示自己作为队长的团队

## 注意事项

1. **数据库变更**：无需数据库迁移，`username` 字段即为用户昵称
2. **用户名不可修改**：用户名是登录凭证，不允许修改
3. **权限控制**：
   - 用户只能修改自己的个人信息
   - 只有队长才能转让队长身份
   - 只有队长才能添加/移除成员
4. **性能考虑**：搜索功能使用数据库查询，在大量用户时仍能保持良好性能

## 技术栈

- **后端**：Spring Boot 3.2, JPA, MySQL
- **前端**：Vue 3, Element Plus, Pinia
- **数据库**：MySQL 8.0+

## 文件变更列表

### 后端文件：
- `src/main/java/com/competition/entity/User.java` - 保持原样，无需添加nickname字段
- `src/main/java/com/competition/dto/TeamMemberDetailDTO.java` - 新增DTO（移除了nickname字段）
- `src/main/java/com/competition/controller/UserController.java` - 添加更新接口
- `src/main/java/com/competition/controller/TeamController.java` - 添加多个新接口
- `src/main/java/com/competition/service/UserService.java` - 添加更新方法（移除了nickname相关代码）
- `src/main/java/com/competition/service/TeamService.java` - 添加多个新方法
- `src/main/java/com/competition/repository/UserRepository.java` - 添加搜索方法（仅搜索username）

### 前端文件：
- `frontend/src/views/UserProfile.vue` - 添加编辑功能（移除了nickname编辑，username显示为"用户名(昵称)"）
- `frontend/src/views/Teams.vue` - 大幅更新，添加多个新功能（使用username作为昵称显示）
- `frontend/src/views/ApplyCompetition.vue` - 添加团队选择功能
- `frontend/src/api/index.js` - 添加新API调用

### 数据库文件：
- 无需数据库迁移脚本
