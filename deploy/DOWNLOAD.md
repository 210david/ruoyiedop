# 环境组件下载清单

> 下载后将文件解压到对应目录，最终整个 `D:\EDOP` 目录拷贝到服务器即可运行。
> 所有脚本已内置环境变量配置，**不需要在服务器上配置任何系统环境变量**。

---

## 1. JDK 17（便携版）

| 项目 | 内容 |
|------|------|
| 下载地址 | https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip |
| 备选地址 | https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64&package=jdk |
| 下载文件 | `jdk-17_windows-x64_bin.zip`（约 170MB） |
| 解压到 | `D:\EDOP\jdk17\` |

**验证**：解压后确保以下文件存在
```
D:\EDOP\jdk17\bin\java.exe
D:\EDOP\jdk17\bin\javaw.exe
```

**解压步骤**：
1. 下载 `jdk-17_windows-x64_bin.zip`
2. 解压后会得到 `jdk-17.0.x` 文件夹
3. 将文件夹内容直接放入 `D:\EDOP\jdk17\`（确保 `bin` 目录直接在 `jdk17` 下）

---

## 2. Redis（Windows版）

| 项目 | 内容 |
|------|------|
| 下载地址 | https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip |
| 下载文件 | `Redis-x64-5.0.14.1.zip`（约 4MB） |
| 解压到 | `D:\EDOP\redis\` |

**验证**：解压后确保以下文件存在
```
D:\EDOP\redis\redis-server.exe
D:\EDOP\redis\redis-cli.exe
```

**注意**：解压后删除自带的 `redis.windows.conf`，使用我们提供的配置文件：
```
将 deploy\config\redis.windows.conf 复制到 D:\EDOP\redis\redis.windows.conf
```

---

## 3. Nginx（Windows版）

| 项目 | 内容 |
|------|------|
| 下载地址 | http://nginx.org/download/nginx-1.24.0.zip |
| 下载文件 | `nginx-1.24.0.zip`（约 1.5MB） |
| 解压到 | `D:\EDOP\nginx\` |

**验证**：解压后确保以下文件存在
```
D:\EDOP\nginx\nginx.exe
D:\EDOP\nginx\conf\nginx.conf
```

**注意**：解压后用我们的配置文件替换默认配置：
```
将 deploy\config\nginx.conf 覆盖到 D:\EDOP\nginx\conf\nginx.conf
```

---

## 4. WinSW（可选 - 用于注册Windows服务开机自启）

| 项目 | 内容 |
|------|------|
| 下载地址 | https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe |
| 下载文件 | `WinSW-x64.exe`（约 17.3MB） |
| 放置位置 | `D:\EDOP\service\` |

**操作**：下载后复制四份，分别重命名：
```
D:\EDOP\service\edop-mysql.exe
D:\EDOP\service\edop-backend.exe
D:\EDOP\service\edop-nginx.exe
D:\EDOP\service\edop-redis.exe
```

> WinSW 是可选的。如果不注册服务，用 `start-all.bat` 手动启动即可。

---

## 5. MySQL 8.0（便携版）

| 项目 | 内容 |
|------|------|
| 下载地址 | https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.42-winx64.zip |
| 下载文件 | `mysql-8.0.42-winx64.zip`（约 233MB） |
| 解压到 | `D:\EDOP\mysql\` |

**验证**：解压后确保以下文件存在
```
D:\EDOP\mysql\bin\mysqld.exe
D:\EDOP\mysql\bin\mysql.exe
```

**注意**：解压后将我们的配置文件复制到 MySQL 目录：
```
将 deploy\config\my.ini 复制到 D:\EDOP\mysql\my.ini
```

**首次使用必须初始化**（生成 data 目录和临时 root 密码）：
```batch
cd D:\EDOP\scripts
init-mysql.bat
start-mysql.bat
set-mysql-password.bat
```

> MySQL 便携版与安装版功能完全一致，数据存储在 `D:\EDOP\mysql\data\`，拷贝到服务器后运行 `init-mysql.bat` 即可。

---

## 最终目录结构验证

下载解压完成后，`D:\EDOP` 目录结构应如下：

```
D:\EDOP\
├── jdk17\
│   └── bin\
│       ├── java.exe          ✅
│       └── javaw.exe         ✅
├── mysql\
│   ├── bin\
│   │   ├── mysqld.exe        ✅
│   │   └── mysql.exe         ✅
│   ├── my.ini                ✅（用我们的配置）
│   ├── data\                 （init-mysql.bat 生成）
│   └── logs\                 （自动创建）
├── redis\
│   ├── redis-server.exe      ✅
│   ├── redis-cli.exe         ✅
│   ├── redis.windows.conf    ✅（用我们的配置覆盖）
│   ├── data\                 （自动创建）
│   └── logs\                 （自动创建）
├── nginx\
│   ├── nginx.exe             ✅
│   ├── conf\
│   │   └── nginx.conf        ✅（用我们的配置覆盖）
│   └── logs\                 （自动创建）
├── app\
│   ├── ruoyi-admin.jar       ✅（build.bat 编译生成）
│   ├── application.yml       ✅（从 config 复制）
│   ├── application-druid.yml ✅（从 config 复制）
│   └── logs\                 （自动创建）
├── frontend\
│   └── dist\                 ✅（build.bat 编译生成）
│       └── index.html
├── uploadPath\               （自动创建）
├── service\                  （可选）
│   ├── edop-mysql.exe        ✅（WinSW 重命名）
│   ├── edop-mysql.xml        ✅
│   ├── edop-backend.exe      ✅（WinSW 重命名）
│   ├── edop-backend.xml      ✅
│   ├── edop-nginx.exe        ✅
│   ├── edop-nginx.xml        ✅
│   ├── edop-redis.exe        ✅
│   └── edop-redis.xml        ✅
├── scripts\
│   ├── env.bat               ✅（核心：定义所有环境变量）
│   ├── start-all.bat
│   └── ...
└── config\
    ├── application.yml      ✅
    ├── application-druid.yml ✅
    ├── my.ini               ✅
    ├── nginx.conf           ✅
    ├── redis.windows.conf   ✅
    └── sql\
        └── EDOP_ALL.sql     ✅（合并版SQL，一条命令导入全部）
```

---

## 为什么不需要配置系统环境变量？

`env.bat` 文件在每个脚本启动时会自动设置：
- `JAVA_HOME=D:\EDOP\jdk17`
- `MYSQL_HOME=D:\EDOP\mysql`
- `PATH=...\jdk17\bin;...`

所有脚本开头都有 `call env.bat`，所以 java、mysql、redis、nginx 命令都能自动找到，完全不需要在 Windows 系统里配置环境变量。

**所有环境组件（JDK、MySQL、Redis、Nginx）均已便携化，整个 `D:\EDOP` 目录拷贝到服务器即可运行。**
