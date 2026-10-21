# ===================================================
#  RuoYi-Vue Dockerfile - 多阶段构建
#  使用方法：
#    docker build -t ruoyi-server:3.9.2 .
#    docker run -d -p 8080:8080 --name ruoyi-server ruoyi-server:3.9.2
# ===================================================

# ==================== 第一阶段：Maven 编译 ====================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY ruoyi-common/pom.xml ruoyi-common/
COPY ruoyi-system/pom.xml ruoyi-system/
COPY ruoyi-framework/pom.xml ruoyi-framework/
COPY ruoyi-quartz/pom.xml ruoyi-quartz/
COPY ruoyi-generator/pom.xml ruoyi-generator/
COPY ruoyi-admin/pom.xml ruoyi-admin/

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -q -B

# 复制源码并编译
COPY ruoyi-common/src ruoyi-common/src
COPY ruoyi-system/src ruoyi-system/src
COPY ruoyi-framework/src ruoyi-framework/src
COPY ruoyi-quartz/src ruoyi-quartz/src
COPY ruoyi-generator/src ruoyi-generator/src
COPY ruoyi-admin/src ruoyi-admin/src

RUN mvn clean package -DskipTests -q

# ==================== 第二阶段：运行镜像 ====================
FROM openjdk:17-slim

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用目录
WORKDIR /opt/ruoyi

# 创建日志目录
RUN mkdir -p /opt/ruoyi/logs

# 从构建阶段复制 jar 包
COPY --from=builder /build/ruoyi-admin/target/ruoyi-admin.jar /opt/ruoyi/ruoyi-admin.jar

# 暴露端口
EXPOSE 8080

# JVM 参数（可通过环境变量覆盖）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/ruoyi/logs/"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/ruoyi/ruoyi-admin.jar"]
