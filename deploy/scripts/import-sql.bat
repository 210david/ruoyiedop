@echo off
chcp 65001 >nul
title 企业数字化运营平台 - 导入数据库

cd /d "%~dp0"

echo ============================================================
echo   企业数字化运营平台 - 导入数据库
echo ============================================================
echo.

echo [INFO] 此脚本将导入 EDOP_ALL.sql 到 ry-vue 数据库
set /p "confirm=确定要继续吗？(y/N): "
if /i "%confirm%" NEQ "y" (
    echo [INFO] 操作已取消
    pause
    exit /b 0
)

echo.
set /p "mysql_user=请输入MySQL用户名 (默认: root): "
if "%mysql_user%"=="" set mysql_user=root

echo.
echo 导入中... 请稍候，这可能需要几分钟...
echo.

REM 使用 mysql 客户端执行 SQL 文件
mysql -u %mysql_user% -p < "%~dp0..\config\sql\EDOP_ALL.sql"
if %errorlevel% equ 0 (
    echo.
    echo ============================================================
    echo   数据库导入成功！
    echo ============================================================
    echo.
    echo  请确保已修改配置文件:
    echo    D:\EDOP\app\application-druid.yml
    echo    - url: jdbc:mysql://localhost:3306/ry-vue
    echo    - username: %mysql_user%
    echo    - password: (你刚才输入的密码)
    echo.
) else (
    echo.
    echo [ERROR] 数据库导入失败！
    echo.
    echo 可能原因:
    echo   1. MySQL 未安装或未启动
    echo   2. 用户名/密码错误
    echo   3. ry-vue 数据库已存在且包含数据
    echo.
    echo 解决方法:
    echo   - 确保 MySQL 服务已启动
    echo   - 手动删除 ry-vue 数据库: DROP DATABASE IF EXISTS \`ry-vue\`;
    echo   - 重试此脚本
    echo.
)

pause
