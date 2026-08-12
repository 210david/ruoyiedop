@echo off
chcp 65001 >nul
echo === Executing qms_all.sql ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue < "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\qms_all.sql"
if %errorlevel% neq 0 (
    echo FAILED: qms_all.sql execution failed with error %errorlevel%
    exit /b 1
)
echo SUCCESS: qms_all.sql executed
