@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ===================================================
rem  RuoYi-Vue MySQL 数据库自动备份脚本 (Windows)
rem  功能：备份 ry-vue 数据库，自动清理30天前的旧备份
rem ===================================================

rem --- 配置区域 ---
set DB_HOST=localhost
set DB_PORT=3306
set DB_USER=root
set DB_PASS=123456
set DB_NAME=ry-vue

rem 获取脚本所在目录的上级目录作为项目根目录
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..
set BACKUP_DIR=%PROJECT_DIR%\backup\db

rem 创建备份目录（如果不存在）
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    echo [信息] 已创建备份目录: %BACKUP_DIR%
)

rem 生成备份文件名（格式：ry-vue_backup_YYYYMMDD_HHmmss.sql）
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set timestamp=%datetime:~0,8%_%datetime:~8,6%
set BACKUP_FILE=%BACKUP_DIR%\ry-vue_backup_%timestamp%.sql

echo.
echo ========================================
echo   RuoYi-Vue 数据库备份
echo ========================================
echo [时间] %date% %time%
echo [数据库] %DB_NAME%@%DB_HOST%:%DB_PORT%

rem 执行 mysqldump 备份
echo [信息] 正在备份数据库...
mysqldump -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --single-transaction --routines --triggers --databases %DB_NAME% > "%BACKUP_FILE%" 2>nul

if %errorlevel% equ 0 (
    echo [成功] 数据库备份完成！
    echo [文件] %BACKUP_FILE%

    rem 计算文件大小
    for %%A in ("%BACKUP_FILE%") do set filesize=%%~zA
    set /a filesize_kb=filesize/1024
    echo [大小] !filesize_kb! KB
) else (
    echo [失败] 数据库备份出错，请检查：
    echo       1. mysqldump 是否已添加到系统PATH环境变量
    echo       2. MySQL 服务是否正常运行
    echo       3. 数据库连接参数是否正确（%DB_USER%@%DB_HOST%:%DB_PORT%）
    goto :cleanup
)

rem --- 清理30天前的旧备份 ---
echo.
echo [信息] 正在清理30天前的旧备份...
forfiles /p "%BACKUP_DIR%" /m "ry-vue_backup_*.sql" /d -30 /c "cmd /c del @file" 2>nul
if %errorlevel% equ 0 (
    echo [信息] 旧备份清理完成（保留最近30天的备份）
) else (
    echo [信息] 没有需要清理的旧备份文件
)

:cleanup
echo.
echo ========================================
echo   备份任务结束
echo ========================================
echo.

endlocal
pause
