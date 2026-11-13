# -*- coding: utf-8 -*-
"""打包 ZIP：D:/EDOP -> D:/EDOP/YLSP_full_20261104.zip（排除 backend/logs 与 uploadPath）"""
import zipfile, os
src = r'D:/EDOP/YLSP'
out = r'D:/EDOP/YLSP_full_20261104.zip'
def skip(p):
    parts = p.replace(os.sep, '/').lower()
    return ('/backend/logs' in parts) or ('/uploadpath' in parts)
z = zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED)
n = 0
for root, dirs, files in os.walk(src):
    if skip(root):
        dirs[:] = []
        continue
    for f in files:
        fp = os.path.join(root, f)
        z.write(fp, os.path.relpath(fp, src))
        n += 1
z.close()
z = zipfile.ZipFile(out)
bad = z.testzip()
z.close()
print('files:', n)
print('bad_entry:', bad)
print('size:', os.path.getsize(out))
