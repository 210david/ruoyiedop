#!/bin/bash
# ===================================================
#  RuoYi-Vue 一键部署脚本 (Linux)
#  功能：环境检查、编译、停止旧进程、启动、配置systemd
# ===================================================

set -e

# --- 配置区域 ---
SERVER_PORT=8080
ACCESS_URL="http://localhost:${SERVER_PORT}"
JAVA_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/ruoyi/logs/"
APP_NAME="ruoyi-admin"
SERVICE_NAME="ruoyi"

# 获取脚本所在目录的上级目录作为项目根目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
DEPLOY_DIR="${PROJECT_DIR}/deploy"
LOG_DIR="${PROJECT_DIR}/logs"
JAR_FILE="${PROJECT_DIR}/ruoyi-admin/target/ruoyi-admin.jar"

echo ""
echo "========================================"
echo "  RuoYi-Vue 一键部署系统"
echo "========================================"
echo "[时间] $(date '+%Y-%m-%d %H:%M:%S')"
echo "[项目路径] ${PROJECT_DIR}"
echo ""

# ========================================
#  步骤0：检查环境依赖
# ========================================
echo "--- 步骤0/6：检查环境依赖 ---"

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "[失败] 未检测到 Java，请安装 JDK 8+ 或 17"
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | grep -i version | head -1 | awk -F '"' '{print $2}')
echo "[通过] Java 版本: ${JAVA_VER}"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "[失败] 未检测到 Maven，请先安装 Maven"
    exit 1
fi
echo "[通过] Maven 已安装: $(mvn -v 2>&1 | head -1)"

echo ""

# ========================================
#  步骤1：编译项目
# ========================================
echo "--- 步骤1/6：编译后端项目 ---"
cd "${PROJECT_DIR}"

echo "[信息] 执行 mvn clean package -DskipTests ..."
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "[失败] 项目编译失败，请查看上方错误信息"
    exit 1
fi
echo "[成功] 项目编译完成"

# 检查 jar 包
if [ ! -f "${JAR_FILE}" ]; then
    echo "[失败] 未找到编译产物: ${JAR_FILE}"
    exit 1
fi
echo ""

# ========================================
#  步骤2：停止旧进程
# ========================================
echo "--- 步骤2/6：停止旧进程 ---"

# 先尝试使用 systemd 停止
if systemctl is-active --quiet ${SERVICE_NAME} 2>/dev/null; then
    echo "[信息] 停止 systemd 服务: ${SERVICE_NAME}"
    systemctl stop ${SERVICE_NAME}
    echo "[成功] systemd 服务已停止"
fi

# 查找并终止占用端口的进程
OLD_PID=$(lsof -ti:${SERVER_PORT} 2>/dev/null || netstat -tlnp 2>/dev/null | grep ":${SERVER_PORT} " | awk '{print $7}' | cut -d'/' -f1)
if [ -n "$OLD_PID" ]; then
    echo "[信息] 终止占用端口 ${SERVER_PORT} 的进程 PID: ${OLD_PID}"
    kill -15 ${OLD_PID} 2>/dev/null
    sleep 3
    # 如果还没死，强制杀
    if kill -0 ${OLD_PID} 2>/dev/null; then
        kill -9 ${OLD_PID} 2>/dev/null
        echo "[警告] 已强制终止进程"
    fi
    echo "[成功] 旧进程已停止"
else
    echo "[信息] 端口 ${SERVER_PORT} 未被占用"
fi
echo ""

# ========================================
#  步骤3：准备部署目录
# ========================================
echo "--- 步骤3/6：准备部署目录 ---"

if [ ! -d "${DEPLOY_DIR}" ]; then
    mkdir -p "${DEPLOY_DIR}"
fi
if [ ! -d "${LOG_DIR}" ]; then
    mkdir -p "${LOG_DIR}"
fi

echo "[信息] 复制 jar 包到部署目录..."
cp -f "${JAR_FILE}" "${DEPLOY_DIR}/ruoyi-admin.jar"
echo "[成功] jar 包已就绪: ${DEPLOY_DIR}/ruoyi-admin.jar"
echo ""

# ========================================
#  步骤4：启动服务
# ========================================
echo "--- 步骤4/6：启动服务 ---"
echo "[信息] JVM 参数: ${JAVA_OPTS}"

cd "${DEPLOY_DIR}"

# 启动应用
nohup java ${JAVA_OPTS} -jar ruoyi-admin.jar >> "${LOG_DIR}/server.log" 2>&1 &
NEW_PID=$!
echo "[信息] 服务进程 PID: ${NEW_PID}"

# 等待启动
echo "[信息] 等待服务启动..."
for i in $(seq 1 30); do
    if curl -s -o /dev/null "http://127.0.0.1:${SERVER_PORT}"; then
        echo "[成功] 服务启动成功！"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "[警告] 服务在30秒内未响应，请检查日志: ${LOG_DIR}/server.log"
    fi
    sleep 1
done
echo ""

# ========================================
#  步骤5：配置 systemd 自动启动
# ========================================
echo "--- 步骤5/6：配置 systemd 服务 ---"

SERVICE_FILE="${SCRIPT_DIR}/ruoyi.service"
if [ -f "${SERVICE_FILE}" ]; then
    cp -f "${SERVICE_FILE}" /etc/systemd/system/${SERVICE_NAME}.service
    systemctl daemon-reload
    systemctl enable ${SERVICE_NAME} 2>/dev/null
    echo "[成功] systemd 服务已配置: /etc/systemd/system/${SERVICE_NAME}.service"
    echo "[提示] 管理命令："
    echo "  - 启动：systemctl start ${SERVICE_NAME}"
    echo "  - 停止：systemctl stop ${SERVICE_NAME}"
    echo "  - 重启：systemctl restart ${SERVICE_NAME}"
    echo "  - 状态：systemctl status ${SERVICE_NAME}"
    echo "  - 日志：journalctl -u ${SERVICE_NAME} -f"
else
    echo "[警告] 未找到 ruoyi.service 文件，跳过 systemd 配置"
    echo "[提示] 可手动复制 bin/ruoyi.service 到 /etc/systemd/system/"
fi
echo ""

# ========================================
#  部署完成
# ========================================
echo "========================================"
echo "  部署完成！"
echo "========================================"
echo "访问地址：${ACCESS_URL}"
echo "日志文件：${LOG_DIR}/server.log"
echo "Druid 监控：${ACCESS_URL}/druid"
echo "Swagger 文档：${ACCESS_URL}/swagger-ui.html"
echo ""
echo "管理命令："
echo "  systemctl start ${SERVICE_NAME}    # 启动服务"
echo "  systemctl stop ${SERVICE_NAME}     # 停止服务"
echo "  systemctl restart ${SERVICE_NAME}  # 重启服务"
echo "  systemctl status ${SERVICE_NAME}   # 查看状态"
echo "========================================"
echo ""

exit 0
