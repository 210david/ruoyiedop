# -*- coding: utf-8 -*-
"""清理包目录 dist 中的孤儿文件：以仓库最新构建 dist 为基准，逐文件删除多余旧文件"""
import os
repo = r'D:/EDOP_QY/EDOP_YLSP/ruoyi-ui/dist'
pkg = r'D:/EDOP/YLSP/frontend/dist'

def rel_files(root):
    out = {}
    for root2, dirs, files in os.walk(root):
        for f in files:
            fp = os.path.join(root2, f)
            out[os.path.relpath(fp, root).replace(os.sep, '/')] = fp
    return out

repo_files = rel_files(repo)
pkg_files = rel_files(pkg)
orphans = [k for k in pkg_files if k not in repo_files]
missing = [k for k in repo_files if k not in pkg_files]
print('orphans:', len(orphans), 'missing_in_pkg:', len(missing))
removed = 0
for k in sorted(orphans):
    try:
        os.remove(pkg_files[k])
        removed += 1
    except Exception as e:
        print('FAIL', k, e)
print('removed:', removed)
# 清理空目录
for root2, dirs, files in os.walk(pkg, topdown=False):
    try:
        if not os.listdir(root2):
            os.rmdir(root2)
    except OSError:
        pass
# 复核
pkg_files2 = rel_files(pkg)
still = [k for k in pkg_files2 if k not in repo_files]
missing2 = [k for k in repo_files if k not in pkg_files2]
print('still_orphan:', len(still), 'missing_in_pkg:', len(missing2))
if still[:5]: print('still sample:', still[:5])
if missing2[:5]: print('missing sample:', missing2[:5])
