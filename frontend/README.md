# 竞赛管理系统 - 前端

基于 Vue 3 + Vite + Element Plus 的竞赛管理系统前端应用。

## 技术栈

- **框架**: Vue 3
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **UI 框架**: Element Plus
- **HTTP 客户端**: Axios

## 功能模块

### 公开功能
- **竞赛列表**: 浏览所有竞赛信息
- **用户注册**: 新用户注册（注册后需要管理员审核激活）
- **用户登录**: 已激活用户登录系统

### 学生功能（需登录）
- **我的报名**: 查看报名状态，已审核通过的报名可提交作品
- **我的作品**: 创建、编辑、提交作品
- **竞赛详情**: 查看竞赛详细信息并报名

### 管理员功能
- **用户管理**: 审核激活新注册用户、暂停/删除用户
- **竞赛管理**: 创建、编辑、删除竞赛
- **报名审核**: 审核学生报名申请
- **评审管理**: 查看评审分配和评分
- **奖项管理**: 创建奖项、授予奖项

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 开发环境运行

```bash
npm run dev
```

应用将在 http://localhost:5173 启动

### 生产环境构建

```bash
npm run build
```

构建产物将输出到 `dist` 目录

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API 接口
│   │   ├── axios.js    # Axios 配置
│   │   └── index.js    # API 方法
│   ├── assets/         # 资源文件
│   ├── components/     # 公共组件
│   ├── router/         # 路由配置
│   │   └── index.js
│   ├── stores/         # Pinia 状态管理
│   │   └── auth.js     # 认证状态
│   ├── views/          # 页面组件
│   │   ├── admin/      # 管理员页面
│   │   │   ├── Users.vue          # 用户管理
│   │   │   ├── Competitions.vue   # 竞赛管理
│   │   │   ├── Registrations.vue  # 报名审核
│   │   │   ├── Judges.vue         # 评审管理
│   │   │   └── Awards.vue         # 奖项管理
│   │   ├── Admin.vue              # 管理后台布局
│   │   ├── Login.vue              # 登录页
│   │   ├── Register.vue           # 注册页
│   │   ├── Competitions.vue       # 竞赛列表
│   │   ├── CompetitionDetail.vue  # 竞赛详情
│   │   ├── MyRegistrations.vue    # 我的报名
│   │   └── MySubmissions.vue      # 我的作品
│   ├── App.vue         # 根组件
│   ├── main.js         # 入口文件
│   └── style.css       # 全局样式
├── index.html
├── package.json
└── vite.config.js      # Vite 配置
```

## 配置说明

### API 代理配置

前端通过 Vite 代理将 `/api` 请求转发到后端服务器。配置在 `vite.config.js`:

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 认证机制

- 使用 JWT Token 进行身份认证
- Token 存储在 localStorage 中
- 请求拦截器自动在请求头添加 Token
- 响应拦截器处理 401 未授权错误

## 用户注册流程（重要）

### 新用户注册
1. 用户填写注册表单（用户名、密码、邮箱、学校等信息）
2. 提交注册后，账号状态为 **"未激活"(inactive)**
3. 系统提示：**"注册成功，请等待管理员审核激活账号"**
4. 此时用户**无法登录**，需要等待管理员审核

### 管理员审核激活
1. 管理员登录后进入 **"管理后台 > 用户管理"**
2. 点击 **"待审核用户"** 查看所有未激活用户
3. 审核用户信息后，点击 **"激活"** 按钮
4. 用户状态变为 **"激活"(active)**，用户即可登录使用系统

### 用户状态说明
- **inactive (未激活)**: 新注册用户的默认状态，无法登录
- **active (激活)**: 管理员审核通过后的状态，可以正常登录使用
- **suspended (暂停)**: 管理员可以暂停已激活的用户

## 开发注意事项

1. **后端依赖**: 前端需要后端服务运行在 http://localhost:8080
2. **权限控制**: 部分路由需要登录或管理员权限
3. **数据格式**: 所有 API 响应使用统一格式 `{ success, message, data }`
4. **日期时间**: 使用 ISO 8601 格式

## 浏览器兼容性

支持所有现代浏览器：
- Chrome (推荐)
- Firefox
- Safari
- Edge

## License

MIT License
