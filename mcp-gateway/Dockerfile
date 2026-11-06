# EDOP MCP 网关镜像（客户服务器 Linux/Docker 交付用）
# 构建镜像时只打包代码与清单，profiles/ 真实凭据通过挂载卷注入，不进镜像
FROM node:22-alpine

WORKDIR /app

# 先装依赖（利用层缓存）
COPY package.json package-lock.json ./
RUN npm ci --registry=https://registry.npmmirror.com --omit=dev

# 再拷代码（gateway.mjs / tool-manifests/ / profiles/standard.example.json 等）
COPY . .

# profiles 真实配置运行时挂载：-v /opt/edop/mcp-gateway/profiles:/app/profiles
ENV EDOP_GATEWAY_MODE=http
EXPOSE 3000

CMD ["node", "gateway.mjs"]
