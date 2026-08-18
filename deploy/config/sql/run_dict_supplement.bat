@echo off
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "source e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/deploy/config/sql/mms_dict_supplement.sql"
echo EXIT_CODE: %ERRORLEVEL%
