# 企业数字化运营平台 - Windows服务器部署指南

## 一、部署目录结构

所有环境和服务统一放在 `D:\EDOP` 目录下：

```
D:\EDOP\
├── jdk17\                  # JDK 17（便携版）
├── mysql\                  # MySQL 8.0（便携版）
│   ├── bin\
│   │   ├── mysqld.exe
│   │   └── mysql.exe
│   ├── data\               # 数据目录（初始化后生成）
│   ├── logs\               # 日志目录
│   └── my.ini              # MySQL配置文件
├── redis\                  # Redis
│   ├── redis-server.exe
│   ├── redis-cli.exe
│   ├── redis.windows.conf  # Redis配置文件
│   ├── data\               # 数据目录
│   └── logs\               # 日志目录
├── nginx\                  # Nginx
│   ├── nginx.exe
│   ├── conf\
│   │   └── nginx.conf      # Nginx配置文件
│   ├── logs\               # 日志目录
│   └── html\
├── app\                    # 后端应用
│   ├── ruoyi-admin.jar     # 后端JAR包
│   ├── application.yml     # 主配置文件
│   ├── application-druid.yml # 数据源配置
│   └── logs\               # 日志目录
├── frontend\               # 前端静态文件
│   └── dist\
│       ├── index.html
│       ├── static\
│       └── assets\
├── uploadPath\             # 文件上传目录
├── service\                # Windows服务配置（WinSW）
│   ├── edop-mysql.exe      # WinSW（MySQL服务）
│   ├── edop-mysql.xml
│   ├── edop-backend.exe    # WinSW（后端服务）
│   ├── edop-backend.xml
│   ├── edop-nginx.exe
│   ├── edop-nginx.xml
│   ├── edop-redis.exe
│   └── edop-redis.xml
├── scripts\                # 管理脚本
│   ├── env.bat             # 公共环境变量
│   ├── init-dirs.bat       # 初始化目录
│   ├── init-mysql.bat      # 初始化MySQL（首次必须运行）
│   ├── build.bat           # 编译打包
│   ├── start-all.bat       # 一键启动（MySQL+Redis+后端+Nginx）
│   ├── stop-all.bat        # 一键停止
│   ├── status.bat          # 查看状态
│   ├── start-mysql.bat     # 启动MySQL
│   ├── stop-mysql.bat      # 停止MySQL
│   ├── set-mysql-password.bat # 修改MySQL root密码
│   ├── start-redis.bat     # 启动Redis
│   ├── stop-redis.bat      # 停止Redis
│   ├── start-backend.bat   # 启动后端
│   ├── stop-backend.bat    # 停止后端
│   ├── start-nginx.bat     # 启动Nginx
│   ├── stop-nginx.bat      # 停止Nginx
│   ├── reload-nginx.bat    # 重载Nginx配置
│   ├── import-sql.bat      # 导入数据库
│   ├── check-deployment.bat # 检查部署状态
│   ├── install-services.bat   # 注册Windows服务
│   └── uninstall-services.bat # 卸载Windows服务
└── config\                 # 配置文件模板
    ├── application.yml
    ├── application-druid.yml
    ├── my.ini              # MySQL配置模板
    ├── nginx.conf
    ├── redis.windows.conf
    └── sql\
        └── EDOP_ALL.sql    # 合并版SQL（一条命令导入全部）
```

---

## 二、环境下载清单

| 组件 | 版本 | 下载地址 | 放置位置 |
|------|------|----------|----------|
| JDK 17 | 17.x | https://www.oracle.com/java/technologies/downloads/ | `D:\EDOP\jdk17\` |
| Redis | 5.0+ | https://github.com/tporadowski/redis/releases | `D:\EDOP\redis\` |
| Nginx | 1.24+ | http://nginx.org/en/download.html | `D:\EDOP\nginx\` |
| MySQL | 8.0.42 | https://dev.mysql.com/downloads/mysql/ | `D:\EDOP\mysql\` |
| WinSW | 2.x+ | https://github.com/winsw/winsw/releases | `D:\EDOP\service\` |

> **JDK 便携版说明**：下载 ZIP 版本解压即可，无需安装。确保 `D:\EDOP\jdk17\bin\java.exe` 存在。

> **Redis Windows版**：推荐使用 tporadowski 维护的 Windows 版 Redis 5.0.14.1。

> **MySQL 便携版**：下载 `mysql-8.0.42-winx64.zip`（ZIP Archive），解压到 `D:\EDOP\mysql\`，无需安装程序。首次使用运行 `init-mysql.bat` 初始化。

> **WinSW**：下载 `WinSW-x64.exe`，复制四份分别重命名为 `edop-mysql.exe`、`edop-backend.exe`、`edop-nginx.exe`、`edop-redis.exe`，放到 `D:\EDOP\service\` 目录。

---

## 三、部署步骤

### 步骤1：复制部署文件

将 `deploy` 目录下的所有内容复制到服务器 `D:\` 盘：

```batch
xcopy /E /I /Y deploy D:\EDOP
```

### 步骤2：初始化目录

双击运行 `D:\EDOP\scripts\init-dirs.bat`，自动创建所有必要的目录结构。

### 步骤3：放入环境组件

#### 3.1 JDK 17
- 下载 JDK 17 ZIP 版
- 解压到 `D:\EDOP\jdk17\`
- 确保 `D:\EDOP\jdk17\bin\java.exe` 存在

#### 3.2 Redis
- 下载 Redis Windows 版 ZIP
- 解压到 `D:\EDOP\redis\`
- 确保 `D:\EDOP\redis\redis-server.exe` 存在
- 将 `D:\EDOP\config\redis.windows.conf` 复制到 `D:\EDOP\redis\`

#### 3.3 Nginx
- 下载 Nginx Windows 版 ZIP
- 解压到 `D:\EDOP\nginx\`
- 确保 `D:\EDOP\nginx\nginx.exe` 存在
- 将 `D:\EDOP\config\nginx.conf` 复制到 `D:\EDOP\nginx\conf\`

#### 3.4 MySQL 8.0（便携版）
- 下载 MySQL 8.0 Community Server ZIP Archive（`mysql-8.0.42-winx64.zip`）
- 解压到 `D:\EDOP\mysql\`
- 确保 `D:\EDOP\mysql\bin\mysqld.exe` 存在
- 将 `D:\EDOP\config\my.ini` 复制到 `D:\EDOP\mysql\`
- **首次使用必须运行** `D:\EDOP\scripts\init-mysql.bat` 初始化数据库
- 运行 `set-mysql-password.bat` 修改 root 密码

#### 3.5 WinSW（可选，用于注册Windows服务）
- 下载 `WinSW-x64.exe`
- 复制四份到 `D:\EDOP\service\`，分别重命名为：
  - `edop-mysql.exe`
  - `edop-backend.exe`
  - `edop-nginx.exe`
  - `edop-redis.exe`

### 步骤4：初始化 MySQL 并导入数据

#### 4.1 初始化 MySQL（仅首次）

```batch
cd D:\EDOP\scripts
init-mysql.bat
```

此脚本会：
1. 复制 `my.ini` 配置文件到 MySQL 目录
2. 创建 `data` 和 `logs` 目录
3. 初始化数据库（生成临时 root 密码）

#### 4.2 启动 MySQL

```batch
start-mysql.bat
```

#### 4.3 修改 root 密码

```batch
set-mysql-password.bat
```

#### 4.4 导入数据库

双击运行 `D:\EDOP\scripts\import-sql.bat`，输入 MySQL 用户名和密码即可自动导入。

#### 方式B：手动导入

```batch
mysql -u root -p < D:\EDOP\config\sql\EDOP_ALL.sql
```

> **说明**：`EDOP_ALL.sql` 已合并所有表（ry基础表 + quartz定时任务表 + wms仓库管理表 + dms设备管理表），
> 并包含 `CREATE DATABASE` 和 `USE` 语句，一条命令即可完成全部导入。

### 步骤5：编译打包

在**开发机**上运行 `D:\EDOP\scripts\build.bat`（需要已安装 Maven 和 Node.js）：

```batch
cd D:\EDOP\scripts
build.bat
```

此脚本会：
1. Maven 编译后端，生成 `ruoyi-admin.jar`
2. npm 编译前端，生成 `dist` 目录
3. 自动复制到部署目录

> **手动打包**：如果脚本中的源码路径不对，请编辑 `build.bat` 中的 `SOURCE_DIR` 变量。

### 步骤6：修改配置文件

#### 6.1 修改数据库配置
编辑 `D:\EDOP\app\application-druid.yml`：

```yaml
spring:
    datasource:
        druid:
            master:
                url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
                username: root
                password: 你的数据库密码
```

#### 6.2 修改Redis配置（可选）
编辑 `D:\EDOP\redis\redis.windows.conf`，如需设置密码：

```conf
requirepass 你的Redis密码
```

同步修改 `D:\EDOP\app\application.yml`：

```yaml
spring:
  data:
    redis:
      password: 你的Redis密码
```

### 步骤7：启动服务

#### 方式A：脚本启动（手动）

```batch
cd D:\EDOP\scripts
start-all.bat
```

#### 方式B：注册Windows服务（推荐，开机自启）

```batch
cd D:\EDOP\scripts
install-services.bat
```

注册后可通过 `services.msc` 管理服务，或使用命令：

```batch
net start EDOP-MySQL
net start EDOP-Redis
net start EDOP-Backend
net start EDOP-Nginx

net stop EDOP-Nginx
net stop EDOP-Backend
net stop EDOP-Redis
net stop EDOP-MySQL
```

### 步骤8：验证部署

| 验证项 | 地址 | 说明 |
|--------|------|------|
| 前端页面 | http://localhost:8005 | 应显示登录页面 |
| 后端API | http://localhost:8004 | 应返回404或认证错误（正常） |
| Druid监控 | http://localhost:8005/druid/ | 账号: ruoyi / 123456 |
| Swagger | http://localhost:8005/swagger-ui.html | API文档页面 |
| 默认账号 | admin / admin123 | 登录系统 |

---

## 四、脚本使用说明

| 脚本 | 功能 |
|------|------|
| `init-dirs.bat` | 初始化所有部署目录 |
| `init-mysql.bat` | 初始化 MySQL 数据库（首次必须运行） |
| `start-mysql.bat` | 启动 MySQL 服务 |
| `stop-mysql.bat` | 停止 MySQL 服务 |
| `set-mysql-password.bat` | 修改 MySQL root 密码 |
| `build.bat` | 编译打包前后端项目 |
| `start-all.bat` | 一键启动 MySQL + Redis + 后端 + Nginx |
| `stop-all.bat` | 一键停止 Nginx + 后端 + Redis + MySQL |
| `status.bat` | 查看所有服务运行状态 |
| `start-redis.bat` | 单独启动 Redis |
| `stop-redis.bat` | 单独停止 Redis |
| `start-backend.bat` | 单独启动后端 |
| `stop-backend.bat` | 单独停止后端 |
| `start-nginx.bat` | 单独启动 Nginx |
| `stop-nginx.bat` | 单独停止 Nginx |
| `reload-nginx.bat` | 重新加载 Nginx 配置（不停止服务） |
| `import-sql.bat` | 导入数据库（EDOP_ALL.sql） |
| `check-deployment.bat` | 检查部署状态和文件完整性 |
| `install-services.bat` | 注册为 Windows 服务（开机自启） |
| `uninstall-services.bat` | 卸载 Windows 服务 |

---

## 五、日志位置

| 日志 | 路径 |
|------|------|
| MySQL 错误日志 | `D:\EDOP\mysql\logs\error.log` |
| MySQL 慢查询日志 | `D:\EDOP\mysql\logs\slow.log` |
| MySQL 初始化日志 | `D:\EDOP\mysql\logs\init.log` |
| 后端应用日志 | `D:\EDOP\app\logs\ruoyi.log` |
| 后端控制台日志 | `D:\EDOP\app\logs\console.log` |
| Nginx 访问日志 | `D:\EDOP\nginx\logs\access.log` |
| Nginx 错误日志 | `D:\EDOP\nginx\logs\error.log` |
| Redis 日志 | `D:\EDOP\redis\logs\redis.log` |
| WinSW 日志 | 各服务目录下的 `winsw\` 目录 |

---

## 六、常见问题

### Q1: 端口被占用
```batch
REM 查看端口占用
netstat -ano | findstr :8004
REM 终止进程
taskkill /F /PID <进程ID>
```

### Q2: 后端启动后无法访问
1. 检查 `application.yml` 和 `application-druid.yml` 配置是否正确
2. 检查 MySQL 是否启动、数据库是否创建、数据是否导入
3. 检查 Redis 是否启动
4. 查看日志：`D:\EDOP\app\logs\console.log`

### Q3: Nginx 502 Bad Gateway
1. 确认后端服务已启动（端口 8004）
2. 检查 `nginx.conf` 中 `upstream` 的地址和端口
3. 防火墙是否阻止了本地端口通信

### Q4: 前端页面空白
1. 确认 `D:\EDOP\frontend\dist\index.html` 存在
2. 检查 `nginx.conf` 中 `root` 路径是否正确
3. 浏览器 F12 控制台查看是否有 JS/CSS 加载失败

### Q5: 文件上传失败
1. 确认 `D:\EDOP\uploadPath` 目录存在且有写入权限
2. 检查 `application.yml` 中 `ruoyi.profile` 配置
3. 检查 `nginx.conf` 中 `/profile/` 的 `alias` 路径

### Q6: Windows服务无法启动
1. 确认 WinSW 的 `.exe` 和 `.xml` 文件在同一目录且名称一致
2. 以管理员身份运行 `install-services.bat`
3. 检查 `xml` 文件中的路径是否正确
4. 查看 Windows 事件查看器中的错误信息

---

## 七、安全建议

1. **修改默认密码**：admin用户密码、MySQL密码、Redis密码、Druid密码
2. **修改Token密钥**：`application.yml` 中的 `token.secret`
3. **关闭Swagger**：生产环境在 `application.yml` 中设 `springdoc.swagger-ui.enabled: false`
4. **关闭Druid监控**：生产环境可设 `statViewServlet.enabled: false`
5. **配置HTTPS**：准备SSL证书，启用 `nginx.conf` 中的 HTTPS 配置
5. **防火墙**：仅开放8005/443端口，关闭8004、3306、6379的外网访问

---

## 八、更新部署

更新版本时：

```batch
REM 1. 停止服务
cd D:\EDOP\scripts
stop-all.bat

REM 2. 重新编译打包
build.bat

REM 3. 启动服务
start-all.bat
```

如果是 Windows 服务模式：

```batch
net stop EDOP-Backend
REM 替换 JAR 包
net start EDOP-Backend
```
