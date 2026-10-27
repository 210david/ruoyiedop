@echo off
chcp 65001 > nul
echo ========================================
echo 安全生产管理模块 - 一键部署脚本
echo ========================================
echo.

cd /d %~dp0

echo [1/5] 执行SQL脚本创建表和菜单...
mysql -h localhost -u root -p123456 ry-vue < sql/safety_all.sql
if %errorlevel% neq 0 (
    echo SQL执行失败，请检查数据库连接
    pause
    exit /b 1
)
echo SQL执行成功
echo.

echo [2/5] 清理并编译项目...
call mvn clean -DskipTests
if %errorlevel% neq 0 (
    echo Maven clean失败
    pause
    exit /b 1
)
echo.

echo [3/5] 打包项目...
call mvn package -DskipTests
if %errorlevel% neq 0 (
    echo Maven package失败
    pause
    exit /b 1
)
echo.

echo [4/5] 部署JAR包...
copy ruoyi-admin\target\ruoyi-admin.jar .\ruoyi-admin.jar /Y
if %errorlevel% neq 0 (
    echo JAR包复制失败
    pause
    exit /b 1
)
echo.

echo [5/5] 启动应用...
echo 启动中，请查看日志...
java -jar ruoyi-admin.jar
pause