<h1 align="center" style="font-weight: bold;">EDOP 企业数字化运营平台</h1>

#### 介绍

EDOP（Enterprise Digital Operation Platform）企业数字化运营平台，基于 RuoYi-Vue 3.9.2（前后端分离架构）深度定制开发，面向中小企业的数字化转型场景，覆盖 **仓储、采购、营销、安全生产、生产管控、质量、人力、财务** 等核心业务模块，并内置 AI 能力（智能问答、流失预警/预测、单据辅助等），帮助企业实现业务流程线上化、数据一体化和管理精细化。

#### 软件架构

| 层级 | 技术栈 | 说明 |
|---|---|---|
| 后端框架 | Spring Boot 4.0.6 / JDK 17 | 前后端分离，RESTful API |
| 持久层 | MyBatis + PageHelper + Druid | 连接池监控 |
| 权限认证 | Spring Security + JWT | 基于角色的菜单/按钮/数据权限 |
| 流程引擎 | Flowable | 审批流程 |
| 前端框架 | Vue 3 + Element Plus + Vite 6.4.1 | 纯 PC 端 |
| 数据库 | MySQL 8.0 | 库名 ry-vue |
| 缓存 | Redis | 会话/字典/参数缓存 |

#### 业务模块

| 模块 | 目录 | 主要功能 |
|---|---|---|
| 营销管理 | ruoyi-mk | 线索管理、客户管理（公海/标签/联系人）、商机管理（看板/阶段配置）、合同订单、价格与返利、活动管理、营销看板、编号规则 |
| 采购管理 | ruoyi-pms | 供应商管理、采购申请/订单、到货与价格比价 |
| 仓储管理 | ruoyi-wms | 出入库管理、库存台账、盘点、库龄预警 |
| 财务管理 | ruoyi-fin | 往来单位、总账凭证、应收/应付、成本、资金账户、财务报表 |
| 人力资源 | ruoyi-hr | 部门/岗位/员工档案管理 |
| 生产管控 | ruoyi-pms | BOM、工艺流程、工作中心、生产工单、报工 |
| 质量管理 | ruoyi-qms | 检验单、质量台账 |
| 安全生产 | ruoyi-safety | 安全检查、隐患排查与闭环管理 |
| 招投标 | ruoyi-bid | 投标项目管理 |
| AI 能力 | ruoyi-ai | 智能问答（nlquery）、AI 流失预警/预测、embedding 检索 |
| 基础平台 | ruoyi-framework / ruoyi-system / ruoyi-common / ruoyi-quartz | 用户/角色/菜单、部门/岗位、字典、参数、日志、定时任务、代码生成 |

#### 目录结构

```
RuoYi-Vue3
├── ruoyi-admin        // 后端服务入口（打包为 edop-admin fat jar）
├── ruoyi-framework    // 框架核心（安全、拦截、数据源）
├── ruoyi-system       // 系统模块（用户/角色/菜单/字典/参数/日志）
├── ruoyi-common       // 通用工具
├── ruoyi-quartz       // 定时任务
├── ruoyi-generator    // 代码生成
├── ruoyi-flowable     // 流程引擎
├── ruoyi-mk           // 营销管理
├── ruoyi-pms          // 采购管理 / 生产管控
├── ruoyi-wms          // 仓储管理
├── ruoyi-fin          // 财务管理
├── ruoyi-hr           // 人力资源
├── ruoyi-qms          // 质量管理
├── ruoyi-safety       // 安全生产
├── ruoyi-bid          // 招投标
├── ruoyi-ai           // AI 能力
├── ruoyi-ui           // 前端（Vue3 + Vite）
├── sql                // 数据库脚本
└── deploy             // 部署配置
```

#### 安装教程

> 环境要求：JDK 17、Maven 3.8+、Node.js 18+、MySQL 8.0、Redis

1. 创建数据库 `ry-vue`（UTF-8），执行 `sql/` 下的初始化脚本
2. 修改 `ruoyi-admin/src/main/resources/application-druid.yml` 中的数据库连接信息，以及 `application.yml` 中的 Redis 配置
3. 后端构建：
   ```bash
   mvn install -DskipTests
   ```
   打包产物：`ruoyi-admin/target/edop-admin-*.jar`
4. 后端启动（默认端口 8081）：
   ```bash
   java -jar ruoyi-admin/target/edop-admin-v10.jar
   ```
5. 前端启动（默认端口 81）：
   ```bash
   cd ruoyi-ui
   npm install
   npm run dev
   ```
6. 浏览器访问：`http://localhost:81`

#### 使用说明

1. 使用管理员账号登录（默认 admin），在【系统管理-用户管理】中创建部门、岗位、人员并分配角色
2. 在【系统管理-菜单管理】中按需启用各业务模块菜单（营销/采购/仓储/生产/质量/安全/人力/财务）
3. 业务初始化顺序：先维护物料、仓库、客户、供应商等基础数据，再进行业务单据操作
4. 生产管控模块需先维护 BOM 与工艺流程，再进行生产工单与报工
5. AI 能力需在【参数设置】中配置大模型 API Key（密钥加密存储，保存后不可查看，留空表示沿用原密钥）
6. 前端打包发布：`npm run build`（建议设置 `NODE_OPTIONS=--max-old-space-size=8192`），产物部署至 Nginx 或后端静态资源目录

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

#### 致谢

- 基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 开源快速开发框架深度定制
- 感谢若依开源社区
