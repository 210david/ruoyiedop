@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue < e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\move_admin.sql
echo ---DONE---
