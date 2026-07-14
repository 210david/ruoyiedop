#!/bin/bash
# ===================================================
#  RuoYi-Vue MySQL 数据库自动备份脚本 (Linux)
#  功能：备份 ry-vue 数据库，自动清理30天前的旧备份
#  使用方法：
#    chmod +x backup-db.sh
#    ./backup-db.sh
#  定时任务示例（每天凌晨2点执行）：
#    crontab -e
#    0 2 * * * /opt/ruoyi/bin/backup-db.sh >> /opt/ruoyi/logs/backup-db.log 2>&1
# ===================================================

# --- 配置区域 ---
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASS="123456"
DB_NAME="ry-vue"

# 获取脚本所在目录的上级目录作为项目根目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="${PROJECT_DIR}/backup/db"

# 创建备份目录（如果不存在）
if [ ! -d "$BACKUP_DIR" ]; then
    mkdir -p "$BACKUP_DIR"
    echo "[信息] 已创建备份目录: ${BACKUP_DIR}"
fi

# 生成备份文件名（格式：ry-vue_backup_YYYYMMDD_HHmmss.sql）
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/ry-vue_backup_${TIMESTAMP}.sql"

echo ""
echo "========================================"
echo "  RuoYi-Vue 数据库备份"
echo "========================================"
echo "[时间] $(date '+%Y-%m-%d %H:%M:%S')"
echo "[数据库] ${DB_NAME}@${DB_HOST}:${DB_PORT}"

# 执行 mysqldump 备份
echo "[信息] 正在备份数据库..."
mysqldump -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" \
    --single-transaction --routines --triggers \
    --databases "${DB_NAME}" > "${BACKUP_FILE}" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "[成功] 数据库备份完成！"
    echo "[文件] ${BACKUP_FILE}"

    # 计算文件大小
    if [ -f "$BACKUP_FILE" ]; then
        FILESIZE=$(stat -f%z "$BACKUP_FILE" 2>/dev/null || stat -c%s "$BACKUP_FILE" 2>/dev/null)
        if [ -n "$FILESIZE" ]; then
            FILESIZE_KB=$((FILESIZE / 1024))
            echo "[大小] ${FILESIZE_KB} KB"
        fi
    fi
else
    echo "[失败] 数据库备份出错，请检查："
    echo "      1. mysqldump 是否已安装（yum install mysql-client 或 apt install mysql-client）"
    echo "      2. MySQL 服务是否正常运行"
    echo "      3. 数据库连接参数是否正确（${DB_USER}@${DB_HOST}:${DB_PORT}）"
    exit 1
fi

# --- 清理30天前的旧备份 ---
echo ""
echo "[信息] 正在清理30天前的旧备份..."
DELETED_COUNT=$(find "${BACKUP_DIR}" -name "ry-vue_backup_*.sql" -type f -mtime +30 -delete -print 2>/dev/null | wc -l)

if [ "$DELETED_COUNT" -gt 0 ]; then
    echo "[信息] 已清理 ${DELETED_COUNT} 个旧备份文件（保留最近30天的备份）"
else
    echo "[信息] 没有需要清理的旧备份文件"
fi

echo ""
echo "========================================"
echo "  备份任务结束"
echo "========================================"
echo ""

exit 0
