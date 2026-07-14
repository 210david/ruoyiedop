@echo off
echo.
echo [消息] 使用优化JVM参数启动RuoYi服务（2核4G服务器优化版）
echo.

cd %~dp0
cd ../ruoyi-admin/target

set JAVA_OPTS=-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC -XX:MaxGCPauseMillis=200
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=../logs/
set JAVA_OPTS=%JAVA_OPTS% -Djava.security.egd=file:/dev/./urandom
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

echo JVM参数: %JAVA_OPTS%
echo.

java %JAVA_OPTS% -jar ruoyi-admin.jar

cd ../bin
pause
