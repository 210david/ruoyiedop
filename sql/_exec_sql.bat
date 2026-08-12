@echo off
echo === Executing qms_trace.sql ===
mysql -u root -p123456 ry-vue < "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\qms_trace.sql"
if %ERRORLEVEL% NEQ 0 (
    echo SQL execution failed
    exit /b 1
)
echo === SQL executed successfully ===
