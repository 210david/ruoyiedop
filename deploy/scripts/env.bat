@echo off
chcp 65001 >nul
REM ============================================================
REM 公共环境变量配置 - 所有脚本通过 call 引入此文件
REM 部署路径: D:\EDOP\scripts\env.bat
REM ============================================================

REM ===== 根目录 =====
set EDOP_HOME=D:\EDOP

REM ===== JDK 路径（使用便携版JDK） =====
set JAVA_HOME=%EDOP_HOME%\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%

REM ===== 各组件路径 =====
set MYSQL_HOME=%EDOP_HOME%\mysql
set REDIS_HOME=%EDOP_HOME%\redis
set NGINX_HOME=%EDOP_HOME%\nginx
set APP_HOME=%EDOP_HOME%\app
set FRONTEND_HOME=%EDOP_HOME%\frontend
set UPLOAD_PATH=%EDOP_HOME%\uploadPath
set LOG_HOME=%EDOP_HOME%\app\logs

REM ===== 服务配置 =====
set JAR_NAME=ruoyi-admin.jar
set BACKEND_PORT=8004
set REDIS_PORT=6379
set NGINX_PORT=8005
set MYSQL_PORT=3306

REM ===== JVM 参数 =====
set JVM_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_HOME%

REM ===== 颜色定义 =====
REM Windows CMD 不支持 ANSI 颜色，用 echo 前缀区分
set INFO=[INFO]
set OK=[OK]
set ERR=[ERROR]
set WARN=[WARN]
