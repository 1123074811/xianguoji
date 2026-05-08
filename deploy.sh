#!/bin/bash

# ============================================
# 鲜果记 - 一键部署脚本
# 适用于 Ubuntu 24.04
# 域名：oujincong.xyz
# 支持全新部署和升级更新
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DOMAIN_NAME="oujincong.xyz"
APP_DIR="/opt/xianguoji"
SERVER_PORT=8080

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "${BLUE}[STEP]${NC} $1"; }

# ============================================
# Nginx 配置生成函数
# ============================================
write_nginx_conf() {
    local domain="$1"
    local port="$2"

    cat > /etc/nginx/sites-available/xianguoji <<EOF
limit_req_zone  \$binary_remote_addr zone=api_limit:10m    rate=30r/s;
limit_req_zone  \$binary_remote_addr zone=login_limit:10m  rate=5r/m;
limit_req_zone  \$binary_remote_addr zone=admin_limit:10m  rate=30r/m;
limit_conn_zone \$binary_remote_addr zone=conn_limit:10m;

# ---- Gzip 压缩 ----
gzip on;
gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_min_length 1024;
gzip_types text/plain text/css text/xml text/javascript
           application/javascript application/x-javascript
           application/json application/xml application/rss+xml
           image/svg+xml font/ttf font/otf application/font-woff
           application/font-woff2;

# ---- 拒绝未知 Host ----
server {
    listen 80 default_server;
    return 444;
}

# ---- HTTP → HTTPS 重定向 ----
server {
    listen 80;
    server_name $domain www.$domain api.$domain m.$domain;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

# ---- API 服务：api.oujincong.xyz ----
server {
    listen 443 ssl http2;
    server_name api.$domain;

    ssl_certificate     /etc/letsencrypt/live/$domain/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$domain/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    client_max_body_size 10m;

    # 隐藏敏感路径
    location ~ /\.git { return 404; }
    location ~ /\.env { return 404; }
    location ~ /actuator/(env|beans|configprops) { return 404; }

    # 登录接口限流
    location /api/u/auth/ {
        limit_req zone=login_limit burst=3 nodelay;
        limit_conn zone=conn_limit 5;
        proxy_pass http://127.0.0.1:$port;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 管理端接口限流
    location /api/admin/ {
        limit_req zone=admin_limit burst=10 nodelay;
        limit_conn zone=conn_limit 10;
        proxy_pass http://127.0.0.1:$port;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 通用 API 代理
    location /api/ {
        limit_req zone=api_limit burst=40 nodelay;
        limit_conn zone=conn_limit 20;
        proxy_pass http://127.0.0.1:$port;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 30s;
    }

    # 上传文件静态访问
    location /static/ {
        alias /data/xianguoji/upload/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 健康检查（仅内网）
    location /actuator/health {
        proxy_pass http://127.0.0.1:$port;
        allow 10.0.0.0/8;
        allow 127.0.0.1;
        deny all;
    }
}

# ---- 商家端 H5：m.oujincong.xyz ----
server {
    listen 443 ssl http2;
    server_name m.$domain www.$domain $domain;

    ssl_certificate     /etc/letsencrypt/live/$domain/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$domain/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    add_header X-Frame-Options SAMEORIGIN always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    client_max_body_size 10m;

    # 带 hash 的静态资源永久缓存
    location ~* ^/assets/.*\.(js|css|woff2?|ttf|otf|eot|svg|png|jpg|jpeg|gif|ico|webp)$ {
        root /var/www/xianguoji-merchant;
        add_header Cache-Control "public, max-age=31536000, immutable";
        add_header Vary "Accept-Encoding";
        gzip_static on;
        access_log off;
        expires 1y;
    }

    # index.html 不缓存
    location = /index.html {
        root /var/www/xianguoji-merchant;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
        expires 0;
    }

    # 商家端前端
    location / {
        root /var/www/xianguoji-merchant;
        index index.html;
        try_files \$uri \$uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
    }

    # API 代理（商家端也需调用后端）
    location /api/ {
        limit_req zone=api_limit burst=40 nodelay;
        limit_conn zone=conn_limit 20;
        proxy_pass http://127.0.0.1:$port;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 30s;
    }

    # 上传文件
    location /static/ {
        alias /data/xianguoji/upload/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

    log_info "已生成 Nginx 配置（api.$domain + m.$domain）"
}

# ============================================
# 主流程
# ============================================
echo "=========================================="
echo "  鲜果记 - 一键部署/升级"
echo "  域名：$DOMAIN_NAME"
echo "=========================================="

# 检测是否为升级模式
IS_UPGRADE=false
if [ -f "$APP_DIR/xianguoji-server/target/xianguoji-server-1.0.0.jar" ] || [ -d "/var/www/xianguoji-merchant" ]; then
    echo ""
    echo "检测到现有部署，是否进行升级？"
    echo "  - 选择 'y'：升级现有部署（保留数据库）"
    echo "  - 选择 'n'：全新部署（会重新初始化数据库）"
    read -p "是否升级 (y/n) [y]: " UPGRADE_OPT
    UPGRADE_OPT=${UPGRADE_OPT:-y}
    if [[ "$UPGRADE_OPT" =~ ^[Yy]$ ]]; then
        IS_UPGRADE=true
        echo ""
        echo "=========================================="
        echo "  模式：升级现有部署"
        echo "  说明：将保留数据库，仅更新前后端代码"
        echo "=========================================="
    else
        echo ""
        echo "=========================================="
        echo "  模式：全新部署"
        echo "  说明：将重新初始化数据库"
        echo "=========================================="
    fi
fi

# 自动获取服务器公网 IP
echo ""
log_info "正在自动获取服务器公网 IP..."
AUTO_IP=""
for service in "https://api.ipify.org" "https://ifconfig.me/ip" "https://icanhazip.com"; do
    AUTO_IP=$(curl -s --max-time 5 "$service" 2>/dev/null | tr -d '[:space:]')
    if [[ "$AUTO_IP" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        break
    fi
    AUTO_IP=""
done

if [[ -n "$AUTO_IP" ]]; then
    echo "检测到公网 IP：$AUTO_IP"
    read -p "是否使用此 IP？(y/n) [y]: " USE_AUTO_IP
    USE_AUTO_IP=${USE_AUTO_IP:-y}
    if [[ "$USE_AUTO_IP" =~ ^[Yy]$ ]]; then
        SERVER_IP="$AUTO_IP"
    else
        read -p "请手动输入服务器公网 IP: " SERVER_IP
    fi
else
    log_warn "自动获取 IP 失败，请手动输入"
    read -p "请输入服务器的公网 IP 地址: " SERVER_IP
fi

if [[ ! "$SERVER_IP" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    log_error "无效的 IP 地址格式"
    exit 1
fi

echo ""
echo "=========================================="
echo "  服务器 IP: $SERVER_IP"
echo "  域名: $DOMAIN_NAME"
echo "  API: api.$DOMAIN_NAME"
echo "  商家端: m.$DOMAIN_NAME"
echo "=========================================="
echo ""

# 检查 root
if [ "$EUID" -ne 0 ]; then
    log_error "请使用 root 用户运行此脚本"
    exit 1
fi

# 加载 .env
ENV_FILE="$APP_DIR/.env"
if [ ! -f "$ENV_FILE" ]; then
    ENV_FILE="$SCRIPT_DIR/.env"
fi
if [ ! -f "$ENV_FILE" ]; then
    log_error "未找到配置文件：$APP_DIR/.env 或 $SCRIPT_DIR/.env"
    log_info "请先复制 .env.example 为 .env 并填入真实值"
    exit 1
fi

# 规范化 .env 行尾
if grep -q $'\r' "$ENV_FILE"; then
    sed -i 's/\r$//' "$ENV_FILE"
fi

set -a
source "$ENV_FILE"
set +a

# 交互填充缺失的关键变量
if [ -z "${MYSQL_PASSWORD:-}" ]; then
    read -s -p "MySQL root 密码: " MYSQL_PASSWORD
    echo ""
    export MYSQL_PASSWORD
fi
if [ -z "${REDIS_PASSWORD:-}" ]; then
    read -s -p "Redis 密码: " REDIS_PASSWORD
    echo ""
    export REDIS_PASSWORD
fi
if [ -z "${JWT_SECRET:-}" ] && [ -z "${JWT_SECRET_V1:-}" ]; then
    read -s -p "JWT 密钥（至少32字节）: " JWT_SECRET
    echo ""
    export JWT_SECRET
fi
if [ -z "${WECHAT_APPID:-}" ]; then
    read -p "微信小程序 AppID: " WECHAT_APPID
    export WECHAT_APPID
fi
if [ -z "${WECHAT_SECRET:-}" ]; then
    read -s -p "微信小程序 Secret: " WECHAT_SECRET
    echo ""
    export WECHAT_SECRET
fi

# 检查必要变量
REQUIRED_VARS=(MYSQL_PASSWORD REDIS_PASSWORD)
for var_name in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var_name}" ]; then
        log_error ".env 中缺少必要配置：$var_name"
        exit 1
    fi
done

# ---- Step 1: 安装系统依赖 ----
log_step "1. 安装系统依赖..."
apt-get update
apt-get install -y \
    maven \
    nodejs \
    nginx \
    openjdk-17-jdk \
    docker.io \
    docker-compose-v2 \
    curl \
    wget \
    unzip \
    certbot \
    python3-certbot-nginx
if ! command -v npm >/dev/null 2>&1; then
    apt-get install -y npm
fi
log_info "系统依赖安装完成"

# ---- Step 2: 部署代码到服务器目录 ----
log_step "2. 部署代码..."
mkdir -p "$APP_DIR"

# 如果脚本所在目录就是项目根目录，直接使用；否则从 git clone
if [ -f "$SCRIPT_DIR/docker-compose.yml" ]; then
    log_info "从本地项目目录部署"
    rsync -a --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='.idea' --exclude='.env' "$SCRIPT_DIR/" "$APP_DIR/"
else
    log_info "从 GitHub 拉取代码"
    if [ ! -d "$APP_DIR/.git" ]; then
        git clone git@github.com:1123074811/xianguoji.git "$APP_DIR"
    else
        cd "$APP_DIR" && git pull origin main
    fi
fi

# 写入 .env 到部署目录
cp "$ENV_FILE" "$APP_DIR/.env"
log_info "代码部署完成"

# ---- Step 3: 配置 MySQL ----
if [ "$IS_UPGRADE" = "false" ]; then
    log_step "3. 配置 MySQL 数据库..."

    # 检查是否使用 Docker 内 MySQL 或系统 MySQL
    USE_DOCKER_MYSQL=true
    read -p "MySQL 部署方式：1) Docker 容器  2) 系统原生安装 [1]: " MYSQL_CHOICE
    MYSQL_CHOICE=${MYSQL_CHOICE:-1}

    if [[ "$MYSQL_CHOICE" == "2" ]]; then
        USE_DOCKER_MYSQL=false
        apt-get install -y mysql-server
        systemctl start mysql
        systemctl enable mysql

        # 配置数据库用户和库
        mysql -u root <<SQLEOF
CREATE DATABASE IF NOT EXISTS xianguoji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON xianguoji.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
SQLEOF

        # 允许远程连接
        sed -i 's/^bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf 2>/dev/null || true
        systemctl restart mysql
        sleep 5
        log_info "系统 MySQL 配置完成"
    else
        log_info "将使用 Docker 容器运行 MySQL（在 docker-compose 中配置）"
    fi
else
    log_step "3. 跳过数据库配置（升级模式）..."
fi

# ---- Step 4: 导入数据库 ----
if [ "$IS_UPGRADE" = "false" ]; then
    log_step "4. 导入数据库..."

    if [ "$USE_DOCKER_MYSQL" = "true" ]; then
        # Docker MySQL 会在首次启动时自动导入 schema.sql
        log_info "Docker MySQL 将在首次启动时自动导入 schema.sql"
    else
        TABLE_COUNT=$(mysql -u root -p"$MYSQL_PASSWORD" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='xianguoji'" 2>/dev/null || echo 0)
        if [ "$TABLE_COUNT" -gt 0 ]; then
            log_info "检测到数据库已有表，跳过数据导入"
        else
            if [ -f "$APP_DIR/database/schema.sql" ]; then
                mysql -u root -p"$MYSQL_PASSWORD" xianguoji < "$APP_DIR/database/schema.sql"
                log_info "数据库 Schema 导入完成"
            else
                log_warn "schema.sql 不存在，跳过导入"
            fi

            # 导入种子数据
            for seed_file in "$APP_DIR"/database/migrations/*.sql; do
                if [ -f "$seed_file" ]; then
                    mysql -u root -p"$MYSQL_PASSWORD" xianguoji < "$seed_file"
                    log_info "迁移脚本导入完成：$(basename "$seed_file")"
                fi
            done
        fi
    fi
else
    log_step "4. 跳过数据库导入（升级模式）..."
fi

# ---- Step 5: 配置 Redis ----
if [ "$IS_UPGRADE" = "false" ]; then
    log_step "5. 配置 Redis..."

    USE_DOCKER_REDIS=true
    read -p "Redis 部署方式：1) Docker 容器  2) 系统原生安装 [1]: " REDIS_CHOICE
    REDIS_CHOICE=${REDIS_CHOICE:-1}

    if [[ "$REDIS_CHOICE" == "2" ]]; then
        USE_DOCKER_REDIS=false
        apt-get install -y redis-server
        systemctl start redis-server || systemctl start redis || true
        systemctl enable redis-server || systemctl enable redis || true

        REDIS_CONF="/etc/redis/redis.conf"
        if [ -f "$REDIS_CONF" ]; then
            sed -i '/^bind /d' "$REDIS_CONF" 2>/dev/null || true
            echo "bind 127.0.0.1" >> "$REDIS_CONF"
            if ! grep -Fq "requirepass $REDIS_PASSWORD" "$REDIS_CONF"; then
                echo "requirepass $REDIS_PASSWORD" >> "$REDIS_CONF"
            fi
            systemctl restart redis-server || systemctl restart redis || true
        fi
        log_info "系统 Redis 配置完成"
    else
        log_info "将使用 Docker 容器运行 Redis（在 docker-compose 中配置）"
    fi
else
    log_step "5. 跳过 Redis 配置（升级模式）..."
fi

# ---- Step 6: 打包后端 ----
log_step "6. 打包后端项目..."
cd "$APP_DIR/xianguoji-server"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package -DskipTests -Dmaven.test.skip=true
JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" | head -n 1)
if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
    log_info "后端打包完成：$JAR_FILE"
else
    log_error "后端打包失败，未找到 JAR 文件"
    exit 1
fi

# ---- Step 7: 打包商家端前端 ----
log_step "7. 打包商家端前端..."
cd "$APP_DIR/merchant-front"
npm install --registry=https://registry.npmmirror.com

# 生成商家端生产环境配置
cat > .env.production <<ENVEOF
VITE_API_BASE_URL=https://api.$DOMAIN_NAME
VITE_UPLOAD_BASE_URL=https://api.$DOMAIN_NAME/static
ENVEOF

npm run build
mkdir -p /var/www/xianguoji-merchant
if [ -d "dist" ]; then
    cp -r dist/* /var/www/xianguoji-merchant/
    log_info "商家端前端打包完成"
else
    log_error "商家端构建失败，未找到 dist 目录"
    exit 1
fi

# ---- Step 8: 选择部署方式（Docker Compose 或 Systemd） ----
echo ""
log_info "请选择后端部署方式："
echo "  1) Docker Compose（推荐，自动管理 MySQL + Redis + App）"
echo "  2) Systemd 服务（需系统已安装 MySQL 和 Redis）"
read -p "请选择 [1]: " DEPLOY_MODE
DEPLOY_MODE=${DEPLOY_MODE:-1}

if [[ "$DEPLOY_MODE" == "1" ]]; then
    # ---- Docker Compose 部署 ----
    log_step "8. 使用 Docker Compose 部署后端..."

    # 修改 docker-compose 中的 upload domain 为生产域名
    cd "$APP_DIR"

    # 确保上传目录存在
    mkdir -p /data/xianguoji/upload

    # 启动 Docker Compose
    docker compose up -d --build
    log_info "Docker Compose 启动完成"

    # 等待服务就绪
    log_info "等待后端服务启动..."
    for i in $(seq 1 30); do
        if curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/actuator/health 2>/dev/null | grep -q "200\|401"; then
            log_info "后端服务已就绪"
            break
        fi
        if [ $i -eq 30 ]; then
            log_warn "后端服务启动超时，请手动检查：docker compose logs app"
        fi
        sleep 2
    done
else
    # ---- Systemd 部署 ----
    log_step "8. 使用 Systemd 部署后端..."

    # 生成生产配置
    if [ ! -f "$APP_DIR/application-prod-override.yml" ]; then
        cat > "$APP_DIR/application-prod-override.yml" <<PRODYML
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/xianguoji?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: $MYSQL_PASSWORD
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: $REDIS_PASSWORD

xianguoji:
  jwt:
    secret: ${JWT_SECRET:-}
    active-kid: v1
    secrets:
      v1: ${JWT_SECRET_V1:-$JWT_SECRET}
  upload:
    base-dir: /data/xianguoji/upload
    domain: https://api.$DOMAIN_NAME/static
  wechat:
    appid: $WECHAT_APPID
    secret: $WECHAT_SECRET
  security:
    cors:
      allowed-origins: https://m.$DOMAIN_NAME,https://$DOMAIN_NAME,https://www.$DOMAIN_NAME
PRODYML
    fi

    # 创建 systemd 服务
    if [ ! -f "/etc/systemd/system/xianguoji.service" ]; then
        cat > /etc/systemd/system/xianguoji.service <<'SYSTEMDEOF'
[Unit]
Description=XianGuoJi Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/xianguoji
ExecStart=/usr/bin/java -server -Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom -jar /opt/xianguoji/xianguoji-server/target/xianguoji-server-1.0.0.jar --spring.profiles.active=prod --spring.config.additional-location=file:/opt/xianguoji/application-prod-override.yml
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=xianguoji

[Install]
WantedBy=multi-user.target
SYSTEMDEOF
    fi

    mkdir -p /data/xianguoji/upload

    systemctl daemon-reload
    if [ "$IS_UPGRADE" = "true" ]; then
        systemctl restart xianguoji
        log_info "后端服务重启完成"
    else
        systemctl start xianguoji
        systemctl enable xianguoji
        log_info "后端服务创建完成"
    fi

    # 等待服务就绪
    log_info "等待后端服务启动..."
    for i in $(seq 1 30); do
        if curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/actuator/health 2>/dev/null | grep -q "200\|401"; then
            log_info "后端服务已就绪"
            break
        fi
        if [ $i -eq 30 ]; then
            log_warn "后端服务启动超时，请手动检查：journalctl -u xianguoji -f"
        fi
        sleep 2
    done
fi

# ---- Step 9: 配置 Nginx 和 SSL ----
if [ "$IS_UPGRADE" = "false" ]; then
    log_step "9. 配置 Nginx 和 SSL 证书..."

    # 验证域名解析
    log_info "正在验证域名解析..."
    for sub in "" "api." "m."; do
        FULL_DOMAIN="${sub}${DOMAIN_NAME}"
        if ping -c 1 -W 3 "$FULL_DOMAIN" >/dev/null 2>&1; then
            log_info "$FULL_DOMAIN 解析正常"
        else
            log_warn "$FULL_DOMAIN 解析失败或超时，请检查 DNS 配置"
        fi
    done

    echo ""
    echo "请确保已配置以下 DNS 解析："
    echo "  - A 记录：@ → $SERVER_IP"
    echo "  - A 记录：api → $SERVER_IP"
    echo "  - A 记录：m → $SERVER_IP"
    echo "  - A 记录：www → $SERVER_IP"
    echo ""
    read -p "DNS 解析是否已配置？(y/n) [y]: " DNS_READY
    DNS_READY=${DNS_READY:-y}

    if [[ "$DNS_READY" =~ ^[Yy]$ ]]; then
        # 申请 SSL 证书（通配符证书需要 DNS 验证，这里用多域名）
        log_info "正在申请 SSL 证书..."
        certbot certonly --nginx \
            -d "$DOMAIN_NAME" \
            -d "www.$DOMAIN_NAME" \
            -d "api.$DOMAIN_NAME" \
            -d "m.$DOMAIN_NAME" \
            --non-interactive \
            --agree-tos \
            --email "admin@$DOMAIN_NAME" \
            || {
                log_warn "Let's Encrypt 证书申请失败，尝试逐个域名申请..."
                certbot certonly --nginx \
                    -d "$DOMAIN_NAME" \
                    -d "api.$DOMAIN_NAME" \
                    --non-interactive \
                    --agree-tos \
                    --email "admin@$DOMAIN_NAME" \
                    || log_error "SSL 证书申请失败，请手动配置"
            }
        log_info "SSL 证书申请完成"
    fi

    # 生成 Nginx 配置
    write_nginx_conf "$DOMAIN_NAME" "$SERVER_PORT"

    # 移除默认配置，启用鲜果记配置
    rm -f /etc/nginx/sites-enabled/default
    ln -sf /etc/nginx/sites-available/xianguoji /etc/nginx/sites-enabled/xianguoji

    # 测试并重启 Nginx
    nginx -t
    systemctl restart nginx
    systemctl enable nginx
    log_info "Nginx 配置完成"
else
    log_step "9. 重启 Nginx（升级模式）..."
    nginx -t
    systemctl restart nginx
    log_info "Nginx 重启完成"
fi

# ---- Step 10: 配置数据库备份定时任务 ----
if [ "$IS_UPGRADE" = "false" ]; then
    log_step "10. 配置数据库备份..."
    mkdir -p /opt/xianguoji/backups
    chmod +x "$APP_DIR/xianguoji-server/scripts/backup-db.sh" 2>/dev/null || true

    # 添加 crontab
    CRON_LINE="0 3 * * * $APP_DIR/xianguoji-server/scripts/backup-db.sh >> /opt/xianguoji/backups/backup.log 2>&1"
    if ! crontab -l 2>/dev/null | grep -q "backup-db.sh"; then
        (crontab -l 2>/dev/null; echo "$CRON_LINE") | crontab -
        log_info "数据库备份定时任务已配置（每天凌晨 3:00）"
    else
        log_info "数据库备份定时任务已存在"
    fi
else
    log_step "10. 跳过备份配置（升级模式）..."
fi

# ---- Step 11: 检查服务状态 ----
log_step "11. 检查服务状态..."
echo ""
echo "=========================================="
echo "  服务状态检查"
echo "=========================================="

if [[ "$DEPLOY_MODE" == "1" ]]; then
    echo "Docker 容器状态："
    docker compose -f "$APP_DIR/docker-compose.yml" ps
    echo ""
else
    services=("xianguoji" "nginx")
    [ "$USE_DOCKER_MYSQL" = "false" ] && services+=("mysql")
    [ "$USE_DOCKER_REDIS" = "false" ] && services+=("redis-server")
    for service in "${services[@]}"; do
        if systemctl is-active --quiet "$service" 2>/dev/null; then
            log_info "✓ $service 运行正常"
        else
            log_warn "✗ $service 未运行"
        fi
    done
fi

# ---- 部署完成 ----
echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo ""
echo "访问地址："
echo "  API 服务：https://api.$DOMAIN_NAME"
echo "  API 文档：https://api.$DOMAIN_NAME/swagger-ui.html（仅 dev 环境）"
echo "  商家端：  https://m.$DOMAIN_NAME"
echo "  商家端：  https://$DOMAIN_NAME"
echo ""
echo "微信小程序配置："
echo "  AppID：$WECHAT_APPID"
echo "  服务器域名：https://api.$DOMAIN_NAME"
echo "  → 在微信公众平台 → 开发管理 → 开发设置 中配置"
echo ""
echo "SSL 证书信息："
echo "  证书类型：Let's Encrypt（免费）"
echo "  证书有效期：90 天"
echo "  自动续期：已配置"
echo "  管理命令：certbot certificates / certbot renew"
echo ""
echo "DNS 解析配置："
echo "  A 记录：@   → $SERVER_IP"
echo "  A 记录：api → $SERVER_IP"
echo "  A 记录：m   → $SERVER_IP"
echo "  A 记录：www → $SERVER_IP"
echo ""
echo "数据库备份："
echo "  备份目录：/opt/xianguoji/backups"
echo "  定时任务：每天 03:00"
echo "  保留天数：30 天"
echo ""
echo "常用运维命令："
if [[ "$DEPLOY_MODE" == "1" ]]; then
    echo "  查看日志：docker compose -f $APP_DIR/docker-compose.yml logs -f app"
    echo "  重启服务：docker compose -f $APP_DIR/docker-compose.yml restart app"
    echo "  停止服务：docker compose -f $APP_DIR/docker-compose.yml down"
else
    echo "  查看日志：journalctl -u xianguoji -f"
    echo "  重启服务：systemctl restart xianguoji"
    echo "  停止服务：systemctl stop xianguoji"
fi
echo "  Nginx 日志：tail -f /var/log/nginx/error.log"
echo "  数据库备份：$APP_DIR/xianguoji-server/scripts/backup-db.sh"
echo ""
