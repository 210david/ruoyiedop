@echo off
echo Clearing MMS dictionary cache from Redis...
redis-cli KEYS "sys_dict:mms_*" > %TEMP%\mms_dict_keys.txt
for /f %%k in (%TEMP%\mms_dict_keys.txt) do (
    redis-cli DEL "%%k" > nul
)
del %TEMP%\mms_dict_keys.txt
echo Done. Verifying remaining mms keys:
redis-cli KEYS "sys_dict:mms_*"
