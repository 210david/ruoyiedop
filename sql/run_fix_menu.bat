@echo off
chcp 65001 >nul
echo === Fixing QMS menu hierarchy ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue < "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\fix_qms_menu_hierarchy.sql"
if %errorlevel% neq 0 (
    echo FAILED: fix script failed
    exit /b 1
)
echo SUCCESS: Menu hierarchy fixed
