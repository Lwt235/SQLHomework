# Competition Status and Review Start Time Enhancement

## 概述 / Overview

本次更新解决了以下问题：
1. 添加了`review_start`字段，使管理员可以指定评审开始时间
2. 实现了更细粒度的竞赛状态（报名中、进行中、评审中、公示中等）
3. 修复了时间轴不更新的问题，使其使用系统时间而非浏览器时间

This update addresses the following issues:
1. Added `review_start` field to allow administrators to specify review start time
2. Implemented more granular competition statuses (registering, submitting, reviewing, publicizing, etc.)
3. Fixed timeline not updating by using system time instead of browser time

## 数据库更改 / Database Changes

### 新增字段 / New Field

在`Competition`表中添加了新字段：
```sql
review_start datetime COMMENT '评审开始时间（作品提交截止后开始评审）'
```

### 迁移脚本 / Migration Script

对于现有数据库，运行以下迁移脚本：
```bash
mysql -u [username] -p [database_name] < src/main/resources/db/migration_add_review_start.sql
```

或手动执行SQL：
```sql
ALTER TABLE Competition 
ADD COLUMN review_start datetime COMMENT '评审开始时间（作品提交截止后开始评审）' 
AFTER submit_start;

UPDATE Competition 
SET review_start = award_publish_start 
WHERE review_start IS NULL AND award_publish_start IS NOT NULL;
```

## 竞赛状态说明 / Competition Status Explanation

### 新状态值 / New Status Values

| 状态值 | 中文名称 | 说明 |
|--------|----------|------|
| `draft` | 草稿 | 竞赛尚未发布 |
| `registering` | 报名中 | 报名阶段，signup_start ≤ now < submit_start |
| `submitting` | 进行中-提交作品 | 作品提交阶段，submit_start ≤ now < (review_start or award_publish_start) |
| `reviewing` | 评审中 | 评审阶段，review_start ≤ now < award_publish_start |
| `publicizing` | 公示中 | 获奖公示阶段，award_publish_start ≤ now < (award_publish_start + 1 day) |
| `finished` | 已结束 | 竞赛已完成 |

### 向后兼容 / Backward Compatibility

系统保留对旧状态值的支持：
- `published` (已发布) - 映射到新的`registering`状态
- `ongoing` (进行中) - 映射到新的`submitting`状态

## 时间字段说明 / Time Fields Explanation

| 字段 | 说明 | 必填 |
|------|------|------|
| `signup_start` | 报名开始时间 | 是 |
| `submit_start` | 作品提交开始时间（同时也是报名截止时间） | 是 |
| `review_start` | 评审开始时间 | 否（可选，如果不填则评审时间不可见） |
| `award_publish_start` | 奖项公示开始时间（同时也是评审结束时间） | 是 |

### 时间顺序验证 / Time Order Validation

系统会验证以下时间顺序：
```
signup_start < submit_start < review_start < award_publish_start < (award_publish_start + 1 day)
```

如果`review_start`为空，则验证：
```
signup_start < submit_start < award_publish_start < (award_publish_start + 1 day)
```

## 管理员操作指南 / Administrator Guide

### 创建竞赛时填写评审时间 / Filling Review Time When Creating Competition

1. 登录管理后台
2. 进入"竞赛管理"页面
3. 点击"创建竞赛"按钮
4. 填写竞赛信息，包括：
   - 报名开始时间
   - 提交开始时间（报名截止时间）
   - **评审开始时间**（新增，可选）
   - 奖项公示开始时间
5. 点击"保存"创建竞赛

### 调整系统时间并刷新竞赛状态 / Adjusting System Time and Refreshing Competition Status

1. 登录管理后台
2. 进入"系统管理 > 时间管理"
3. 启用测试模式并设置测试时间
4. 进入"竞赛管理"页面
5. 竞赛状态会自动更新（每小时自动更新一次）
6. 或者调用API手动刷新：`POST /api/competitions/refresh-status`

### 查看时间轴更新 / Viewing Timeline Updates

1. 进入竞赛详情页面
2. 时间轴会自动使用系统时间（而非浏览器时间）显示当前阶段
3. 刷新页面即可看到最新状态

## API 更改 / API Changes

### 竞赛对象新增字段 / New Field in Competition Object

```json
{
  "competitionId": 1,
  "competitionTitle": "示例竞赛",
  "competitionStatus": "registering",
  "signupStart": "2024-01-01T00:00:00",
  "submitStart": "2024-02-01T00:00:00",
  "reviewStart": "2024-03-01T00:00:00",  // 新增字段
  "awardPublishStart": "2024-04-01T00:00:00",
  ...
}
```

### 手动刷新竞赛状态 / Manual Refresh Competition Status

```http
POST /api/competitions/refresh-status
Authorization: Bearer {jwt_token}
Role: ADMIN
```

响应：
```json
{
  "success": true,
  "message": "Competition statuses refreshed successfully",
  "data": null
}
```

## 前端更改 / Frontend Changes

### 新增UI组件 / New UI Components

1. **管理后台竞赛表单**：添加了"评审开始时间"日期选择器
2. **竞赛状态标签**：更新为显示新的细粒度状态
3. **竞赛详情时间轴**：
   - 现在使用系统时间而非浏览器时间
   - 如果设置了`review_start`，会显示独立的评审阶段
   - 如果未设置`review_start`，评审阶段显示为"内部评审"

### 状态筛选 / Status Filtering

竞赛列表页面的状态筛选器已更新，包含所有新状态值。

## 测试建议 / Testing Recommendations

### 测试场景 1：创建带评审时间的竞赛

1. 创建新竞赛，填写所有时间字段（包括review_start）
2. 验证时间顺序验证是否正常工作
3. 查看竞赛详情，确认时间轴显示正确

### 测试场景 2：系统时间调整和状态更新

1. 创建一个竞赛（设置所有时间点）
2. 使用时间管理功能调整系统时间到报名阶段
3. 刷新竞赛状态，验证状态更新为"registering"
4. 查看竞赛详情，确认时间轴显示当前阶段为"报名阶段"
5. 重复步骤2-4，测试其他时间阶段

### 测试场景 3：向后兼容性

1. 测试现有竞赛（没有review_start）是否正常显示
2. 验证旧状态值（published、ongoing）是否正确映射到新状态

### 测试场景 4：竞赛时间轴刷新

1. 打开竞赛详情页面
2. 在另一个标签页调整系统时间
3. 刷新竞赛详情页面
4. 验证时间轴是否使用系统时间更新

## 注意事项 / Notes

1. **review_start是可选字段**：如果管理员不填写，系统将按照原有逻辑运行（评审阶段不可见）
2. **系统时间优先**：前端时间轴现在优先使用系统时间API返回的时间，而非浏览器本地时间
3. **自动状态更新**：竞赛状态会每小时自动更新一次，或者管理员可以手动触发更新
4. **数据迁移**：现有数据库需要运行迁移脚本添加review_start字段

## 文件更改列表 / Changed Files

### Backend
- `CollegeStudentCompetitionManagementSystem_mysql.sql` - 数据库模式更新
- `src/main/resources/db/migration_add_review_start.sql` - 迁移脚本
- `src/main/java/com/competition/entity/Competition.java` - 实体类
- `src/main/java/com/competition/mapper/CompetitionMapper.java` - 数据访问层
- `src/main/java/com/competition/service/CompetitionService.java` - 业务逻辑层
- `src/main/java/com/competition/service/CompetitionStatusScheduler.java` - 状态调度器

### Frontend
- `frontend/src/views/admin/Competitions.vue` - 管理后台竞赛管理
- `frontend/src/views/CompetitionDetail.vue` - 竞赛详情页面
- `frontend/src/views/Competitions.vue` - 竞赛列表页面
