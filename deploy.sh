#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="${APP_DIR:-/opt/xianguoji}"
SERVER_PORT="${SERVER_PORT:-8080}"
PACKAGE_PATH="${PACKAGE_PATH:-}"

log_info() { echo "[INFO] $1"; }
log_warn() { echo "[WARN] $1"; }
log_error() { echo "[ERROR] $1"; }
log_step() { echo "[$(date '+%H:%M:%S')] $1"; }

require_root() {
  if [ "$(id -u)" -ne 0 ]; then
    log_error "请使用 root 用户运行"
    exit 1
  fi
}

detect_public_ip() {
  local ip=""
  for service in "https://api.ipify.org" "https://ifconfig.me/ip" "https://icanhazip.com"; do
    ip="$(curl -s --max-time 5 "$service" 2>/dev/null | tr -d '[:space:]' || true)"
    if [[ "$ip" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
      echo "$ip"
      return 0
    fi
  done
  return 1
}

mysql_escape() {
  printf "%s" "$1" | sed "s/'/''/g"
}

mysql_root() {
  if mysql -uroot -p"$MYSQL_PASSWORD" -e "SELECT 1" >/dev/null 2>&1; then
    mysql -uroot -p"$MYSQL_PASSWORD" "$@"
  else
    mysql -uroot "$@"
  fi
}

write_backend_env() {
  cat > "$APP_DIR/.env" <<ENVEOF
SPRING_PROFILES_ACTIVE=prod
SERVER_HOST=$SERVER_IP
SERVER_PORT=$SERVER_PORT
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DB=xianguoji
MYSQL_USER=root
MYSQL_PASSWORD=$MYSQL_PASSWORD
MYSQL_SSL=false
MYSQL_REQUIRE_SSL=false
MYSQL_ALLOW_PUBLIC_KEY_RETRIEVAL=true
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=$REDIS_PASSWORD
DRUID_USER=druid
DRUID_PASSWORD=$MYSQL_PASSWORD
JWT_SECRET=$JWT_SECRET
JWT_SECRET_V1=$JWT_SECRET
WECHAT_APPID=$WECHAT_APPID
WECHAT_SECRET=$WECHAT_SECRET
ALIYUN_OSS_ENDPOINT=${ALIYUN_OSS_ENDPOINT:-oss-cn-hangzhou.aliyuncs.com}
ALIYUN_OSS_ACCESS_KEY_ID=${ALIYUN_OSS_ACCESS_KEY_ID:-demo}
ALIYUN_OSS_ACCESS_KEY_SECRET=${ALIYUN_OSS_ACCESS_KEY_SECRET:-demo}
ALIYUN_OSS_BUCKET=${ALIYUN_OSS_BUCKET:-xianguoji-demo}
CORS_ALLOWED_ORIGINS=$CORS_ALLOWED_ORIGINS
UPLOAD_MAX_SIZE_MB=${UPLOAD_MAX_SIZE_MB:-10}
ENVEOF
  chmod 600 "$APP_DIR/.env"
}

write_nginx_conf() {
  local server_name="$1"
  cat > /etc/nginx/sites-available/xianguoji <<NGINXEOF
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name $server_name;
    client_max_body_size 10m;

    root /var/www/xianguoji-user;
    index index.html;

    location ~ /\\. {
        return 404;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:$SERVER_PORT;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:$SERVER_PORT;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 3600s;
    }

    location /static/ {
        alias /data/xianguoji/upload/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location = /merchant {
        return 301 /merchant/;
    }

    location ^~ /merchant/ {
        alias /var/www/xianguoji-merchant/;
        try_files \$uri \$uri/ /merchant/index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
    }

    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
        expires 0;
    }

    location ~* ^/(assets|static)/.*\\.(js|css|woff2?|ttf|otf|eot|svg|png|jpg|jpeg|gif|ico|webp)$ {
        add_header Cache-Control "public, max-age=31536000, immutable";
        expires 1y;
        access_log off;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
    }
}
NGINXEOF
}

require_root

echo "=========================================="
echo "  鲜果记 - 原生一键部署"
echo "=========================================="

SERVER_IP="${SERVER_IP:-}"
if [ -z "$SERVER_IP" ]; then
  AUTO_IP="$(detect_public_ip || true)"
  if [ -n "$AUTO_IP" ]; then
    read -r -p "服务器公网 IP [$AUTO_IP]: " SERVER_IP
    SERVER_IP="${SERVER_IP:-$AUTO_IP}"
  else
    read -r -p "服务器公网 IP: " SERVER_IP
  fi
fi
if [[ ! "$SERVER_IP" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  log_error "无效的 IP 地址：$SERVER_IP"
  exit 1
fi

DOMAIN_NAME="${DOMAIN_NAME:-}"
if [ -z "$DOMAIN_NAME" ] && [ -t 0 ]; then
  read -r -p "部署域名（可留空，默认使用服务器 IP）: " DOMAIN_NAME
fi
ACCESS_HOST="${DOMAIN_NAME:-$SERVER_IP}"
PUBLIC_ORIGIN="http://$ACCESS_HOST"
WS_ORIGIN="ws://$ACCESS_HOST"
NGINX_SERVER_NAME="_"
if [ -n "$DOMAIN_NAME" ]; then
  NGINX_SERVER_NAME="$DOMAIN_NAME www.$DOMAIN_NAME"
fi

MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
if [ -z "$MYSQL_PASSWORD" ] && [ -t 0 ]; then
  read -r -s -p "MySQL root 密码: " MYSQL_PASSWORD
  echo ""
fi
if [ -z "$MYSQL_PASSWORD" ]; then
  log_error "缺少 MYSQL_PASSWORD"
  exit 1
fi
REDIS_PASSWORD="${REDIS_PASSWORD:-$MYSQL_PASSWORD}"
JWT_SECRET="${JWT_SECRET:-$(openssl rand -hex 32)}"
WECHAT_APPID="${WECHAT_APPID:-wxfbb4085f2c6b2992}"
WECHAT_SECRET="${WECHAT_SECRET:-}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-$PUBLIC_ORIGIN,http://$SERVER_IP,http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174}"

log_info "服务器 IP：$SERVER_IP"
log_info "访问地址：$PUBLIC_ORIGIN"
log_info "商家端：$PUBLIC_ORIGIN/merchant/"

log_step "1/10 安装系统依赖"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y openjdk-17-jdk maven nodejs npm mysql-server redis-server nginx curl wget rsync tar

log_step "2/10 部署项目代码"
mkdir -p "$APP_DIR"
if [ -n "$PACKAGE_PATH" ] && [ -f "$PACKAGE_PATH" ]; then
  rm -rf "$APP_DIR"
  mkdir -p "$APP_DIR"
  tar -xzf "$PACKAGE_PATH" -C "$APP_DIR" --strip-components=1
elif [ "$SCRIPT_DIR" != "$APP_DIR" ] && { [ -f "$SCRIPT_DIR/package.json" ] || [ -d "$SCRIPT_DIR/xianguoji-server" ]; }; then
  rsync -a --delete --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='.idea' --exclude='.env' "$SCRIPT_DIR/" "$APP_DIR/"
else
  log_info "使用当前部署目录：$APP_DIR"
fi

log_step "3/10 配置 MySQL"
systemctl enable --now mysql
MYSQL_PASSWORD_SQL="$(mysql_escape "$MYSQL_PASSWORD")"
mysql_root <<SQLEOF
CREATE DATABASE IF NOT EXISTS xianguoji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD_SQL';
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD_SQL';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD_SQL';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD_SQL';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
FLUSH PRIVILEGES;
SQLEOF
TABLE_COUNT="$(mysql -uroot -p"$MYSQL_PASSWORD" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='xianguoji';" 2>/dev/null || echo 0)"
if [ "$TABLE_COUNT" -eq 0 ] && [ -f "$APP_DIR/database/schema.sql" ]; then
  mysql -uroot -p"$MYSQL_PASSWORD" xianguoji < "$APP_DIR/database/schema.sql"
  for seed_file in "$APP_DIR"/database/migrations/*.sql; do
    [ -f "$seed_file" ] && mysql -uroot -p"$MYSQL_PASSWORD" xianguoji < "$seed_file" || true
  done
else
  log_info "数据库已有表，跳过初始化导入"
fi

log_step "4/10 配置 Redis"
systemctl enable --now redis-server || systemctl enable --now redis
REDIS_CONF="/etc/redis/redis.conf"
if [ -f "$REDIS_CONF" ]; then
  sed -i '/^bind /d;/^requirepass /d;/^protected-mode /d' "$REDIS_CONF" || true
  {
    echo "bind 127.0.0.1"
    echo "protected-mode yes"
    echo "requirepass $REDIS_PASSWORD"
  } >> "$REDIS_CONF"
fi
systemctl restart redis-server || systemctl restart redis

log_step "5/10 写入后端环境变量"
mkdir -p /data/xianguoji/upload /var/log/xianguoji
write_backend_env

log_step "6/10 打包后端"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn -f "$APP_DIR/xianguoji-server/pom.xml" clean package -DskipTests -Dmaven.test.skip=true -Ddependency-check.skip=true
JAR_FILE="$(find "$APP_DIR/xianguoji-server/target" -maxdepth 1 -type f -name "*.jar" ! -name "*.original" | head -n 1)"
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
  log_error "后端 Jar 不存在"
  exit 1
fi

log_step "7/10 配置并启动后端 systemd"
cat > /etc/systemd/system/xianguoji.service <<SYSTEMDEOF
[Unit]
Description=XianGuoJi Backend Service
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=root
WorkingDirectory=$APP_DIR
EnvironmentFile=$APP_DIR/.env
ExecStart=/usr/bin/java -server -Xms256m -Xmx512m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -jar $JAR_FILE --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=xianguoji

[Install]
WantedBy=multi-user.target
SYSTEMDEOF
systemctl daemon-reload
systemctl enable xianguoji
systemctl restart xianguoji

for i in $(seq 1 60); do
  code="$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:$SERVER_PORT/actuator/health || true)"
  echo "后端健康检查 $i: $code"
  if echo "$code" | grep -q "200\|401\|403"; then
    break
  fi
  if [ "$i" -eq 60 ]; then
    journalctl -u xianguoji -n 120 --no-pager || true
    exit 1
  fi
  sleep 2
done

log_step "8/10 构建用户端 H5"
cat > "$APP_DIR/user-front/.env.production" <<ENVEOF
VITE_SERVER_HOST=$SERVER_IP
VITE_SERVER_PORT=$SERVER_PORT
VITE_API_BASE=$PUBLIC_ORIGIN
VITE_APP_MODE=demo
VITE_USE_MOCK=false
VITE_CLIENT_TYPE=h5
VITE_ENABLE_WECHAT_LOGIN=false
VITE_ENABLE_WECHAT_PAY=false
VITE_ENABLE_DEMO_LOGIN=true
VITE_DEMO_LOGIN_PHONE=13800000001
VITE_DEMO_LOGIN_CODE=1234
VITE_DEMO_PAY_METHOD=mock
ENVEOF
rm -rf "$APP_DIR/user-front/node_modules"
npm --prefix "$APP_DIR/user-front" install --registry=https://registry.npmmirror.com --legacy-peer-deps
npm --prefix "$APP_DIR/user-front" run build:h5
rm -rf /var/www/xianguoji-user
mkdir -p /var/www/xianguoji-user
cp -r "$APP_DIR/user-front/dist/build/h5/." /var/www/xianguoji-user/

log_step "9/10 构建商家端 Web"
cat > "$APP_DIR/merchant-front/.env.production" <<ENVEOF
VITE_SERVER_HOST=$SERVER_IP
VITE_SERVER_PORT=$SERVER_PORT
VITE_API_BASE=$PUBLIC_ORIGIN
VITE_WS_URL=$WS_ORIGIN
ENVEOF
rm -rf "$APP_DIR/merchant-front/node_modules"
npm --prefix "$APP_DIR/merchant-front" install --registry=https://registry.npmmirror.com --legacy-peer-deps
(cd "$APP_DIR/merchant-front" && npx vite build --base /merchant/)
rm -rf /var/www/xianguoji-merchant
mkdir -p /var/www/xianguoji-merchant
cp -r "$APP_DIR/merchant-front/dist/." /var/www/xianguoji-merchant/

log_step "10/10 配置 Nginx"
write_nginx_conf "$NGINX_SERVER_NAME"
rm -f /etc/nginx/sites-enabled/default /etc/nginx/sites-enabled/xianguoji-ip-demo
ln -sf /etc/nginx/sites-available/xianguoji /etc/nginx/sites-enabled/xianguoji
nginx -t
systemctl enable --now nginx
systemctl restart nginx

echo "=========================================="
echo "部署完成"
echo "用户端 H5：$PUBLIC_ORIGIN"
echo "商家端 Web：$PUBLIC_ORIGIN/merchant/"
echo "后端健康检查：curl http://127.0.0.1:$SERVER_PORT/actuator/health"
echo "=========================================="
