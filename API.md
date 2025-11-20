# API 文档

## 概述
竞赛管理系统 REST API 提供七大核心功能模块的接口。

## 认证说明
除了以下接口外，其他所有接口都需要在请求头中包含 JWT Token：
```
Authorization: Bearer <your_token>
```

公开接口（不需要认证）：
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/register` - 用户注册
- GET `/api/competitions/list` - 获取竞赛列表

## 1. 用户管理模块 (Auth)

### 1.1 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "student001",
  "password": "password123",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "school": "XX大学",
  "department": "计算机学院",
  "studentNo": "2021001"
}
```

### 1.2 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "student001",
  "password": "password123"
}

响应:
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "student001"
  }
}
```

## 2. 竞赛管理模块 (Competition)

### 2.1 获取所有竞赛
```
GET /api/competitions/list
```

### 2.2 获取竞赛详情
```
GET /api/competitions/{id}
```

### 2.3 创建竞赛
```
POST /api/competitions
Content-Type: application/json

{
  "competitionTitle": "2024年大学生创新创业大赛",
  "shortTitle": "创新赛",
  "competitionStatus": "draft",
  "description": "比赛详细介绍...",
  "category": "创新创业",
  "level": "national",
  "organizer": "教育部",
  "startDate": "2024-03-01T00:00:00",
  "endDate": "2024-06-30T23:59:59",
  "signupStart": "2024-01-01T00:00:00",
  "signupEnd": "2024-02-28T23:59:59",
  "submitStart": "2024-03-01T00:00:00",
  "submitEnd": "2024-05-31T23:59:59",
  "reviewStart": "2024-06-01T00:00:00",
  "reviewEnd": "2024-06-20T23:59:59",
  "awardPublishDate": "2024-06-30T00:00:00",
  "maxTeamSize": 5
}
```

### 2.4 更新竞赛
```
PUT /api/competitions/{id}
Content-Type: application/json

{竞赛信息}
```

### 2.5 删除竞赛（软删除）
```
DELETE /api/competitions/{id}
```

### 2.6 按状态查询竞赛
```
GET /api/competitions/status/{status}
状态可选: draft, published, ongoing, finished
```

## 3. 报名管理模块 (Registration)

### 3.1 获取所有报名
```
GET /api/registrations
```

### 3.2 获取报名详情
```
GET /api/registrations/{id}
```

### 3.3 创建报名
```
POST /api/registrations
Content-Type: application/json

个人报名:
{
  "competitionId": 1,
  "userId": 1,
  "remark": "期待参加"
}

团队报名:
{
  "competitionId": 1,
  "teamId": 1,
  "remark": "团队报名"
}
```

### 3.4 更新报名状态（审核）
```
PUT /api/registrations/{id}/status?status=approved&auditUserId=2
状态可选: pending, approved, rejected
```

### 3.5 查询竞赛的所有报名
```
GET /api/registrations/competition/{competitionId}
```

### 3.6 查询用户的所有报名
```
GET /api/registrations/user/{userId}
```

## 4. 作品提交模块 (Submission)

### 4.1 获取所有作品
```
GET /api/submissions
```

### 4.2 获取作品详情
```
GET /api/submissions/{id}
```

### 4.3 创建作品
```
POST /api/submissions
Content-Type: application/json

{
  "registrationId": 1,
  "submissionTitle": "智能校园管理系统",
  "abstractText": "项目摘要..."
}
```

### 4.4 更新作品
```
PUT /api/submissions/{id}
Content-Type: application/json

{
  "submissionTitle": "更新后的标题",
  "abstractText": "更新后的摘要",
  "submissionStatus": "draft"
}
```

### 4.5 提交作品
```
POST /api/submissions/{id}/submit
```

### 4.6 锁定作品（最终提交）
```
POST /api/submissions/{id}/lock
```

### 4.7 查询报名的所有作品
```
GET /api/submissions/registration/{registrationId}
```

## 5. 评审管理模块 (Judge)

### 5.1 获取所有评审分配
```
GET /api/judge/assignments
```

### 5.2 创建评审分配
```
POST /api/judge/assignments
Content-Type: application/json

{
  "userId": 2,
  "submissionId": 1,
  "weight": 1.0,
  "score": 0.0,
  "comment": ""
}
```

### 5.3 更新评审（评分）
```
PUT /api/judge/assignments
Content-Type: application/json

{
  "userId": 2,
  "submissionId": 1,
  "weight": 1.0,
  "score": 85.5,
  "comment": "作品质量较高，创新性强"
}
```

### 5.4 查询评委的所有评审任务
```
GET /api/judge/assignments/judge/{userId}
```

### 5.5 查询作品的所有评审
```
GET /api/judge/assignments/submission/{submissionId}
```

## 6. 成绩奖项模块 (Award)

### 6.1 获取所有奖项
```
GET /api/awards
```

### 6.2 获取奖项详情
```
GET /api/awards/{id}
```

### 6.3 创建奖项
```
POST /api/awards
Content-Type: application/json

{
  "competitionId": 1,
  "awardName": "一等奖",
  "awardLevel": "first",
  "criteriaDescription": "综合评分90分以上"
}
```

### 6.4 更新奖项
```
PUT /api/awards/{id}
Content-Type: application/json

{奖项信息}
```

### 6.5 查询竞赛的所有奖项
```
GET /api/awards/competition/{competitionId}
```

### 6.6 授予奖项
```
POST /api/awards/grant?registrationId=1&awardId=1&certificateNo=CERT2024001
```

### 6.7 查询报名的获奖结果
```
GET /api/awards/results/registration/{registrationId}
```

### 6.8 查询奖项的所有获奖者
```
GET /api/awards/results/award/{awardId}
```

## 响应格式

所有接口统一使用以下响应格式：

成功响应:
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

失败响应:
```json
{
  "success": false,
  "message": "错误信息",
  "data": null
}
```

## 数据库状态字段说明

### 用户状态 (user_status)
- `active`: 激活
- `inactive`: 未激活
- `suspended`: 暂停

### 竞赛状态 (competition_status)
- `draft`: 草稿
- `published`: 已发布
- `ongoing`: 进行中
- `finished`: 已结束

### 报名状态 (registration_status)
- `pending`: 待审核
- `approved`: 已通过
- `rejected`: 已拒绝

### 作品状态 (submission_status)
- `draft`: 草稿
- `submitted`: 已提交
- `locked`: 已锁定

### 竞赛级别 (level)
- `school`: 校级
- `provincial`: 省级
- `national`: 国家级
- `international`: 国际级

## 注意事项

1. 所有日期时间使用 ISO 8601 格式: `2024-01-01T00:00:00`
2. JWT Token 默认有效期为 24 小时
3. 所有 API 支持 CORS，允许跨域访问
4. 数据库配置需要在 `application.properties` 中修改
5. 软删除：删除操作不会真正删除数据，只是将 `is_deleted` 字段设置为 true
