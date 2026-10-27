@echo off
REM ============================================================
REM EDOP - Common Environment Variables
REM Path: D:\EDOP\scripts\env.bat
REM ============================================================

REM ===== Root Directory =====
set EDOP_HOME=D:\EDOP

REM ===== JDK Path (portable JDK) =====
set JAVA_HOME=%EDOP_HOME%\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%

REM ===== Component Paths =====
set MYSQL_HOME=%EDOP_HOME%\mysql
set REDIS_HOME=%EDOP_HOME%\redis
set NGINX_HOME=%EDOP_HOME%\nginx
set APP_HOME=%EDOP_HOME%\app
set FRONTEND_HOME=%EDOP_HOME%\frontend
set UPLOAD_PATH=%EDOP_HOME%\uploadPath
set LOG_HOME=%EDOP_HOME%\app\logs

REM ===== Service Config =====
set JAR_NAME=ruoyi-admin.jar
set BACKEND_PORT=8004
set REDIS_PORT=6379
set NGINX_PORT=8005
set MYSQL_PORT=3306

REM ===== JVM Options =====
set JVM_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_HOME%

REM ===== Prefix Tags =====
set INFO=[INFO]
set OK=[OK]
set ERR=[ERROR]
set WARN=[WARN]
