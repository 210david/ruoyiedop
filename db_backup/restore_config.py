# -*- coding: utf-8 -*-
"""恢复 YLSP 包内配置为优化版（参考 cfsp 包内模板 + YLSP 部署说明口径），并同步修复仓库 deploy 模板"""
import shutil, secrets, re

CFSP = r'D:/EDOP/CFSP/backend/config'
PKG = r'D:/EDOP/YLSP/backend/config'
REPO_DEPLOY = r'D:/EDOP_QY/EDOP_YLSP/deploy/config'

# ---------- 1. application-druid.yml：cfsp 模板 + edop_ylsp ----------
src = CFSP + '/application-druid.yml'
dst = PKG + '/application-druid.yml'
text = open(src, encoding='utf-8').read()
text = text.replace('# 数据源配置（CFSP - 独立库 edop_cfsp）', '# 数据源配置（YLSP - 独立库 edop_ylsp）')
text = text.replace('edop_cfsp', 'edop_ylsp')
open(dst, 'w', encoding='utf-8', newline='\n').write(text)
print('druid done, edop_ylsp in file:', 'edop_ylsp' in text, '| edop_cfsp left:', 'edop_cfsp' in text)

# ---------- 2. application.yml：cfsp 模板 + YLSP 值 ----------
src = CFSP + '/application.yml'
dst = PKG + '/application.yml'
text = open(src, encoding='utf-8').read()
text = text.replace('# 项目相关配置（CFSP 生产环境 - 外部覆盖配置）', '# 项目相关配置（YLSP 生产环境 - 外部覆盖配置）')
text = text.replace('profile: D:/EDOP/CFSP/uploadPath', 'profile: D:/EDOP/YLSP/uploadPath')
text = text.replace('# 服务端口：CFSP 后端 81', '# 服务端口：YLSP 后端 82')
text = text.replace('port: 81', 'port: 82')
text = text.replace('# CFSP 环境使用 db0（YLSP 使用 db1，共用实例互不干扰）\n      database: 0',
                    '# YLSP 环境使用 db1（CFSP 使用 db0，共用实例互不干扰）\n      database: 1')
# Redis 密码：YLSP redis.windows.conf 未启用 requirepass，留空
text = re.sub(r'(# Redis 密码[^\n]*\n      password: )[^\n]+', r'\1', text)
# token secret：新生成随机密钥（旧值不可考，生产首装无影响）
new_secret = secrets.token_urlsafe(48)
text = re.sub(r'(  secret: )[^\n]+', r'\1' + new_secret, text)
# xss urlPatterns 补充 YLSP 启用的 qms 模块
text = text.replace('/mms/*,/hr/*', '/mms/*,/hr/*,/qms/*')
text = text.replace('# 项目相关配置', '# 项目相关配置', 1)
open(dst, 'w', encoding='utf-8', newline='\n').write(text)
print('app.yml done: port82=', 'port: 82' in text, '| db1=', 'database: 1' in text,
      '| ylsp_upload=', 'D:/EDOP/YLSP/uploadPath' in text, '| redis_pwd_empty=', 'password: \n' in text or text.count('password:\n') > 0)

# ---------- 3. 同步修复仓库 deploy 模板（防止以后打包再回退） ----------
# druid：包内优化版回填 ruoyi_ylsp 库名
t = open(dst if False else PKG + '/application-druid.yml', encoding='utf-8').read()
t = t.replace('edop_ylsp', 'ruoyi_ylsp')
t = t.replace('# 数据源配置（YLSP - 独立库 edop_ylsp）', '# 数据源配置（YLSP - 打包模板，库名打包时由 ruoyi_ylsp 替换为 edop_ylsp）')
open(REPO_DEPLOY + '/application-druid.yml', 'w', encoding='utf-8', newline='\n').write(t)
# application.yml：包内版本即为打包目标内容，直接回填模板
shutil.copyfile(PKG + '/application.yml', REPO_DEPLOY + '/application.yml')
print('repo deploy templates updated')

# ---------- 4. 校验 ----------
chk = open(PKG + '/application-druid.yml', encoding='utf-8').read()
print('=== 包内 druid 校验 ===')
for kw in ['edop_ylsp', 'config.decrypt=true', 'fBCDoozBjsSnNKAVF5kdOPGdIVn23UESJuY4uFsiiP5ETlKHBaxTC6itcV3Vjo9BRToDYfHFGp7KFwvZE1n2kw==', 'RSFC8cfmnmfdI5i42nkn']:
    print(kw[:40], '->', kw in chk)
chk2 = open(PKG + '/application.yml', encoding='utf-8').read()
print('=== 包内 application.yml 校验 ===')
for kw in ['port: 82', 'database: 1', 'D:/EDOP/YLSP/uploadPath', 'EDOP2026SecureK3y!', 'enabled: false']:
    print(kw, '->', kw in chk2)
print('明文密码残留(zbrj@2026):', 'zbrj@2026' in chk, '/', 'zbrj@2026' in chk2)
print('新token secret前8位:', new_secret[:8])
