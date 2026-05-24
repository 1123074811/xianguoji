#!/usr/bin/env bash
set -euo pipefail

SERVER_IP="${SERVER_IP:?SERVER_IP is required}"
APP_DIR="/opt/xianguoji"
PACKAGE_PATH="${PACKAGE_PATH:-/tmp/xianguoji-deploy.tar.gz}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
if [ -z "$MYSQL_PASSWORD" ]; then
  read -r -s -p "MySQL/Redis/JWT 演示部署密码: " MYSQL_PASSWORD
  echo ""
fi
REDIS_PASSWORD="${REDIS_PASSWORD:-$MYSQL_PASSWORD}"
JWT_SECRET="${JWT_SECRET:-$(openssl rand -hex 32)}"
WECHAT_APPID="${WECHAT_APPID:-wxfbb4085f2c6b2992}"
WECHAT_SECRET="${WECHAT_SECRET:-}"

log_step() {
  echo "[$(date '+%H:%M:%S')] $1"
}

mysql_admin() {
  if mysql -uroot -p"$MYSQL_PASSWORD" -e "SELECT 1" >/dev/null 2>&1; then
    mysql -uroot -p"$MYSQL_PASSWORD" "$@"
  else
    mysql -uroot "$@"
  fi
}

if [ "$(id -u)" -ne 0 ]; then
  echo "请使用 root 用户运行"
  exit 1
fi

log_step "1/9 安装系统依赖"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y openjdk-17-jdk maven nodejs npm mysql-server redis-server nginx curl wget rsync
if ! command -v npm >/dev/null 2>&1; then
  apt-get install -y npm
fi

log_step "2/9 解压项目"
rm -rf "$APP_DIR"
mkdir -p "$APP_DIR"
tar -xzf "$PACKAGE_PATH" -C "$APP_DIR" --strip-components=1

log_step "3/9 配置 MySQL"
systemctl enable --now mysql
mysql_admin <<SQLEOF
CREATE DATABASE IF NOT EXISTS xianguoji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
ALTER USER 'root'@'%' IDENTIFIED WITH caching_sha2_password BY '$MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
SQLEOF
if [ -f "$APP_DIR/database/schema.sql" ]; then
  TABLE_COUNT=$(mysql -uroot -p"$MYSQL_PASSWORD" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='xianguoji';" 2>/dev/null || echo 0)
  if [ "$TABLE_COUNT" -eq 0 ]; then
    mysql -uroot -p"$MYSQL_PASSWORD" xianguoji < "$APP_DIR/database/schema.sql"
  else
    echo "数据库已有表，跳过 schema.sql 导入"
  fi
fi

log_step "4/9 配置 Redis"
systemctl enable --now redis-server || systemctl enable --now redis
REDIS_CONF="/etc/redis/redis.conf"
if [ -f "$REDIS_CONF" ]; then
  sed -i '/^bind /d' "$REDIS_CONF" || true
  sed -i '/^requirepass /d' "$REDIS_CONF" || true
  sed -i '/^protected-mode /d' "$REDIS_CONF" || true
  echo "bind 127.0.0.1" >> "$REDIS_CONF"
  echo "protected-mode yes" >> "$REDIS_CONF"
  echo "requirepass $REDIS_PASSWORD" >> "$REDIS_CONF"
fi
systemctl restart redis-server || systemctl restart redis

log_step "5/9 写入后端环境变量"
cat > "$APP_DIR/.env" <<ENVEOF
SPRING_PROFILES_ACTIVE=prod
SERVER_HOST=$SERVER_IP
SERVER_PORT=8080
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
ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_ACCESS_KEY_ID=demo
ALIYUN_OSS_ACCESS_KEY_SECRET=demo
ALIYUN_OSS_BUCKET=xianguoji-demo
CORS_ALLOWED_ORIGINS=http://$SERVER_IP,http://localhost:5173,http://127.0.0.1:5173
UPLOAD_MAX_SIZE_MB=10
ENVEOF
chmod 600 "$APP_DIR/.env"
mkdir -p /data/xianguoji/upload /var/log/xianguoji

log_step "6/9 准备后端 Jar"
JAR_FILE=$(find "$APP_DIR/xianguoji-server/target" -maxdepth 1 -type f -name "*.jar" ! -name "*.original" 2>/dev/null | head -n 1 || true)
if [ -z "$JAR_FILE" ]; then
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
  mvn -f "$APP_DIR/xianguoji-server/pom.xml" clean package -DskipTests -Dmaven.test.skip=true
  JAR_FILE=$(find "$APP_DIR/xianguoji-server/target" -maxdepth 1 -type f -name "*.jar" ! -name "*.original" | head -n 1)
fi
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
  echo "后端 Jar 不存在"
  exit 1
fi

log_step "7/9 配置后端 systemd"
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
  code=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/actuator/health || true)
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
VITE_SERVER_PORT=8080
VITE_API_BASE=http://$SERVER_IP
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
VITE_SERVER_PORT=8080
VITE_API_BASE=http://$SERVER_IP
VITE_WS_URL=ws://$SERVER_IP
ENVEOF
rm -rf "$APP_DIR/merchant-front/node_modules"
npm --prefix "$APP_DIR/merchant-front" install --registry=https://registry.npmmirror.com --legacy-peer-deps
(cd "$APP_DIR/merchant-front" && npx vite build --base /merchant/)
rm -rf /var/www/xianguoji-merchant
mkdir -p /var/www/xianguoji-merchant
cp -r "$APP_DIR/merchant-front/dist/." /var/www/xianguoji-merchant/

log_step "10/10 配置 Nginx"
cat > /etc/nginx/sites-available/xianguoji-ip-demo <<NGINXEOF
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;
    client_max_body_size 10m;

    root /var/www/xianguoji-user;
    index index.html;

    location ~ /\. {
        return 404;
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

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /static/ {
        alias /data/xianguoji/upload/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
        expires 0;
    }

    location ~* ^/(assets|static)/.*\.(js|css|woff2?|ttf|otf|eot|svg|png|jpg|jpeg|gif|ico|webp)$ {
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
rm -f /etc/nginx/sites-enabled/default
ln -sf /etc/nginx/sites-available/xianguoji-ip-demo /etc/nginx/sites-enabled/xianguoji-ip-demo
nginx -t
systemctl enable --now nginx
systemctl restart nginx

echo "部署完成：用户端 H5 http://$SERVER_IP"
echo "部署完成：商家端 Web http://$SERVER_IP/merchant/"
echo "后端健康检查：curl http://127.0.0.1:8080/actuator/health"
