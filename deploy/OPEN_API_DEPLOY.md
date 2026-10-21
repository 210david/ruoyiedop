# 新功能部署指南 —— 开放接口(Open API) + 接口文档

> 本文档针对本次新开发的功能：**API Key 管理、开放接口(Open API)、接口文档页面**。
> 如为全新部署，请先阅读 `DEPLOYMENT.md` 完成基础环境搭建，再按本文档补充新功能。

---

## 一、本次新开发变更清单

### 1. 后端 Java 代码（编译进 `ruoyi-admin.jar`）

| 类型 | 文件 | 模块 | 说明 |
|------|------|------|------|
| 新增 | `OpenApiInterceptor.java` | ruoyi-framework | API Key 鉴权拦截器，拦截 `/openapi/**` |
| 修改 | `ResourcesConfig.java` | ruoyi-framework | 注册 OpenApiInterceptor |
| 修改 | `SecurityConfig.java` | ruoyi-framework | `/openapi/**` 加入 Spring Security 白名单 |
| 新增 | `OpenApiApp.java` | ruoyi-system | API Key 应用实体 |
| 新增 | `OpenApiAppMapper.java` + `.xml` | ruoyi-system | API Key Mapper |
| 新增 | `IOpenApiAppService.java` + `Impl` | ruoyi-system | API Key Service |
| 新增 | `OpenApiAppController.java` | ruoyi-admin | API Key 管理控制器（增删改查） |
| 新增 | `OpenWmsController.java` | ruoyi-wms | WMS 开放接口（物料/供应商/仓库/出入库/库存/预警） |
| 新增 | `OpenDmsController.java` | ruoyi-dms | DMS 开放接口（设备/分类/工单/备件/预警） |
| 新增 | `OpenMkController.java` | ruoyi-mk | MK 开放接口（客户/线索/商机/合同/订单/联系人/活动/互动/看板/回款） |

### 2. 前端 Vue 代码（编译进 `dist`）

| 类型 | 文件 | 说明 |
|------|------|------|
| 新增 | `views/system/openapi/` | API Key 管理页面（系统管理子菜单） |
| 新增 | `views/tool/apidoc/` | 业务接口文档页面（WMS+DMS+MK 全接口） |
| 新增 | `views/tool/openapidoc/` | 开放接口文档页面（一个接口一个详情页） |
| 新增 | `api/system/openapi.js` | API Key 管理 API 请求封装 |
| 修改 | `router/index.js` | 新增 `/apidoc`、`/openapi-doc` 免登录独立路由 |
| 修改 | `permission.js` | 白名单加入 `/apidoc`、`/openapi-doc` |

### 3. 数据库 SQL（需在生产库执行）

| 脚本 | 内容 | 菜单ID |
|------|------|--------|
| `sql/openapi.sql` | `openapi_app` 表 + 默认测试Key + "开放接口"管理菜单 + 按钮权限 | 119, 1200-1203 |
| `sql/apidoc_menu.sql` | "接口文档"菜单（业务接口文档） | 118 |
| `sql/openapidoc_menu.sql` | "开放接口文档"菜单 | 120 |

> ⚠️ **注意**：`deploy/config/sql/EDOP_ALL.sql` 中**未包含**上述三个脚本的内容，需单独执行。

---

## 二、部署前准备

### 2.1 确认本地代码完整编译通过

在开发机上执行全量编译：

```batch
cd e:\AIcode\EDOP\RuoYi\RuoYi-Vue3
mvn clean install -DskipTests
```

确认所有模块编译成功（特别是 `ruoyi-system`、`ruoyi-wms`、`ruoyi-dms`、`ruoyi-framework`）。

### 2.2 确认前端编译通过

```batch
cd ruoyi-ui
npm install
npm run build:prod
```

确认 `ruoyi-ui/dist` 目录生成成功。

---

## 三、增量更新部署（生产环境已运行旧版本）

> 适用场景：生产服务器上已部署了 WMS/DMS 业务系统，现在要追加 Open API 和文档功能。

### 步骤1：执行数据库 SQL（在生产服务器上）

连接生产 MySQL，依次执行三个脚本：

```batch
REM 连接生产数据库（替换为实际的生产库地址和密码）
mysql -u root -p生产密码 ry-vue < sql\openapi.sql
mysql -u root -p生产密码 ry-vue < sql\apidoc_menu.sql
mysql -u root -p生产密码 ry-vue < sql\openapidoc_menu.sql
```

**验证 SQL 执行结果：**

```sql
-- 检查表是否创建
SHOW TABLES LIKE 'openapi_app';
DESC openapi_app;

-- 检查菜单是否插入
SELECT menu_id, menu_name, path, component FROM sys_menu WHERE menu_id IN (118, 119, 120);

-- 检查角色权限
SELECT * FROM sys_role_menu WHERE menu_id IN (118, 119, 120, 1200, 1201, 1202, 1203);

-- 检查默认 API Key
SELECT app_id, app_name, app_key, allowed_modules, status FROM openapi_app;
```

预期结果：
- `openapi_app` 表存在
- 3 个菜单记录（118=接口文档, 119=开放接口, 120=开放接口文档）
- 7 条角色菜单关联（admin 角色）
- 1 条默认测试 Key（`test-api-key-2026`）

### 步骤2：编译打包后端 JAR

在开发机上：

```batch
cd e:\AIcode\EDOP\RuoYi\RuoYi-Vue3
mvn clean install -DskipTests
```

生成的 JAR 位于：
```
ruoyi-admin\target\ruoyi-admin.jar
```

### 步骤3：编译打包前端

```batch
cd ruoyi-ui
npm run build:prod
```

生成的静态文件位于：
```
ruoyi-ui\dist\
```

### 步骤4：上传到生产服务器

将以下文件上传到生产服务器 `D:\EDOP`：

| 本地文件 | 服务器目标位置 |
|----------|----------------|
| `ruoyi-admin/target/ruoyi-admin.jar` | `D:\EDOP\app\ruoyi-admin.jar` |
| `ruoyi-ui/dist/` 整个目录 | `D:\EDOP\frontend\dist\` |

**上传方式**（任选其一）：
- SCP/SFTP：`scp ruoyi-admin.jar user@server:/D/EDOP/app/`
- 远程桌面：直接拖拽复制
- FTP/共享文件夹

### 步骤5：重启服务

在生产服务器上：

```batch
REM 方式A：脚本方式
cd D:\EDOP\scripts
stop-backend.bat
REM 确认已替换 JAR 和 dist
start-backend.bat

REM 方式B：Windows 服务方式
net stop EDOP-Backend
REM 确认已替换 JAR 和 dist
net start EDOP-Backend
```

> 前端是静态文件，替换 `dist` 后**无需重启 Nginx**，刷新浏览器即可。
> 如需强制刷新可 `reload-nginx.bat`（非必须）。

### 步骤6：验证

| 验证项 | 操作 | 预期结果 |
|--------|------|----------|
| 登录系统 | `http://生产地址:8005` 用 admin 登录 | 成功登录 |
| 接口文档菜单 | 系统管理 → 接口文档 | 显示 WMS+DMS+MK 全接口文档 |
| 开放接口文档菜单 | 系统管理 → 开放接口文档 | 显示 Open API 文档（左侧导航+详情） |
| 开放接口管理菜单 | 系统管理 → 开放接口 | 显示 API Key 管理列表 |
| 免登录文档访问 | 浏览器直接访问 `http://生产地址:8005/apidoc` | 显示接口文档（无需登录） |
| 免登录文档访问 | 浏览器直接访问 `http://生产地址:8005/openapi-doc` | 显示开放接口文档（无需登录） |
| Open API 调用 | 见下方 curl 测试 | 返回 JSON 数据 |

**Open API 连通性测试：**

```batch
REM 通过 Nginx 代理调用（推荐外部系统使用此方式）
curl -H "X-API-Key: test-api-key-2026" http://生产地址:8005/prod-api/openapi/v1/wms/material/list?pageNum=1^&pageSize=1

REM MK 开放接口测试
curl -H "X-API-Key: test-api-key-2026" "http://生产地址:8005/prod-api/openapi/v1/mk/customer/list?pageNum=1^&pageSize=1"

REM 或直接调用后端（仅限内网测试）
curl -H "X-API-Key: test-api-key-2026" http://生产地址:8004/openapi/v1/wms/material/list?pageNum=1^&pageSize=1
```

---

## 四、全新部署（生产环境为空）

按 `DEPLOYMENT.md` 完成基础部署后，**额外执行**：

```batch
REM 导入新功能 SQL
mysql -u root -p < D:\EDOP\config\sql\openapi.sql
mysql -u root -p < D:\EDOP\config\sql\apidoc_menu.sql
mysql -u root -p < D:\EDOP\config\sql\openapidoc_menu.sql
```

> 全新部署时，JAR 和 dist 已经包含新功能代码，无需额外操作。

---

## 五、Nginx 配置注意事项

### 5.1 外部系统调用 Open API 的路径

当前 Nginx 配置中，后端 API 通过 `/prod-api/` 前缀代理：

```nginx
location /prod-api/ {
    proxy_pass http://edop_backend/;
    ...
}
```

因此**外部系统调用 Open API 有两种方式**：

| 方式 | URL | 适用场景 |
|------|-----|----------|
| 通过 Nginx | `http://生产地址:8005/prod-api/openapi/v1/wms/material/list` | **推荐**，外部系统统一走 Nginx |
| 直连后端 | `http://生产地址:8004/openapi/v1/wms/material/list` | 仅内网测试，生产不开放 8004 端口 |

### 5.2 （可选）为 Open API 配置独立路径

如果希望外部系统直接用 `http://域名/openapi/v1/...` 调用（不加 `/prod-api/` 前缀），
在 `nginx.conf` 的 `server` 块中添加：

```nginx
# ---------- 开放接口代理（可选） ----------
location /openapi/ {
    proxy_pass http://edop_backend/openapi/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

添加后执行 `reload-nginx.bat` 生效。这样外部系统可以直接用：
```
http://生产地址:8005/openapi/v1/wms/material/list
```

---

## 六、生产环境安全配置

### 6.1 替换默认 API Key

`openapi.sql` 中插入了一个默认测试 Key `test-api-key-2026`，**生产环境必须替换**：

```sql
-- 删除测试 Key
DELETE FROM openapi_app WHERE app_key = 'test-api-key-2026';

-- 或修改为正式 Key
UPDATE openapi_app SET app_key = '你的正式API-Key-字符串' WHERE app_key = 'test-api-key-2026';
```

也可登录系统后在「系统管理 → 开放接口」页面中创建新的应用 Key。

### 6.2 关闭免登录文档访问（可选）

如果生产环境不希望文档页面被公网访问，可：

**方式A：前端去掉独立路由**（推荐）
- 删除 `router/index.js` 中 `/apidoc` 和 `/openapi-doc` 路由
- 删除 `permission.js` 白名单中的对应路径
- 重新编译前端

**方式B：Nginx 屏蔽**
```nginx
location /apidoc {
    deny all;
}
location /openapi-doc {
    deny all;
}
```

### 6.3 开放接口安全建议

| 项目 | 建议 |
|------|------|
| API Key 长度 | 至少 32 位随机字符串，如 `UUID` 或 `SecureRandom` 生成 |
| 模块权限 | 按 `wms` 或 `dms` 分配最小权限，避免用 `*` |
| Key 定期轮换 | 建议每季度更换一次 Key |
| HTTPS | 生产环境务必启用 HTTPS，防止 Key 明文传输 |
| 日志审计 | 定期检查 Nginx access.log 中 `/openapi/` 的调用记录 |
| 限流（后期） | 如需防止滥用，可在 Nginx 层加 `limit_req` 限流 |

---

## 七、回滚方案

如需回滚本次新功能：

```sql
-- 1. 回滚数据库
DELETE FROM sys_role_menu WHERE menu_id IN (118, 119, 120, 1200, 1201, 1202, 1203);
DELETE FROM sys_menu WHERE menu_id IN (118, 119, 120, 1200, 1201, 1202, 1203);
DROP TABLE IF EXISTS openapi_app;
```

```batch
REM 2. 回滚代码：用旧版 JAR 和 dist 替换回去
net stop EDOP-Backend
REM 替换为旧版 ruoyi-admin.jar 和 dist
net start EDOP-Backend
```

---

## 八、部署检查清单

部署完成后逐项确认：

- [ ] `openapi_app` 表已创建
- [ ] 3 个菜单（118/119/120）已显示在「系统管理」下
- [ ] admin 登录后能看到并打开三个菜单
- [ ] `/apidoc` 免登录可访问
- [ ] `/openapi-doc` 免登录可访问
- [ ] Open API 用 `X-API-Key` 调用返回数据
- [ ] 无 API Key 调用返回 401
- [ ] 默认测试 Key 已替换为正式 Key
- [ ] 后端日志 `D:\EDOP\app\logs\ruoyi.log` 无报错
