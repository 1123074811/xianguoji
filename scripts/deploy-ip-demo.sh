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
JWT_SECRET="${JWT_SECRET:-xianguoji_demo_jwt_secret_32_bytes_minimum_2026}"
WECHAT_APPID="${WECHAT_APPID:-wxfbb4085f2c6b2992}"
WECHAT_SECRET="${WECHAT_SECRET:-demo_wechat_secret_not_used_in_h5}"

if [ "$(id -u)" -ne 0 ]; then
  echo "请使用 root 用户运行"
  exit 1
fi

apt-get update
apt-get install -y openjdk-17-jdk maven nodejs npm nginx docker.io docker-compose-v2 curl wget rsync
systemctl enable --now docker

rm -rf "$APP_DIR"
mkdir -p "$APP_DIR"
tar -xzf "$PACKAGE_PATH" -C "$APP_DIR" --strip-components=1

cat > "$APP_DIR/.env" <<ENVEOF
MYSQL_USER=root
MYSQL_PASSWORD=$MYSQL_PASSWORD
REDIS_PASSWORD=$REDIS_PASSWORD
DRUID_USER=druid
DRUID_PASSWORD=$MYSQL_PASSWORD
JWT_SECRET=$JWT_SECRET
JWT_SECRET_V1=$JWT_SECRET
WECHAT_APPID=$WECHAT_APPID
WECHAT_SECRET=$WECHAT_SECRET
CORS_ALLOWED_ORIGINS=http://$SERVER_IP,http://localhost:5173,http://127.0.0.1:5173
UPLOAD_MAX_SIZE_MB=10
ENVEOF
chmod 600 "$APP_DIR/.env"

cd "$APP_DIR/xianguoji-server"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package -DskipTests -Dmaven.test.skip=true

cd "$APP_DIR"
docker compose up -d --build

for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/actuator/health || true)
  if echo "$code" | grep -q "200\|401\|403"; then
    echo "后端服务已启动"
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo "后端启动超时，请查看 docker compose logs app"
    docker compose ps
    exit 1
  fi
  sleep 2
done

cd "$APP_DIR/user-front"
cat > .env.production <<ENVEOF
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
npm install --registry=https://registry.npmmirror.com
npm run build:h5
rm -rf /var/www/xianguoji-user
mkdir -p /var/www/xianguoji-user
cp -r dist/build/h5/* /var/www/xianguoji-user/

mkdir -p /data/xianguoji/upload
cat > /etc/nginx/sites-available/xianguoji-ip-demo <<NGINXEOF
server {
    listen 80 default_server;
    server_name _;
    client_max_body_size 10m;

    root /var/www/xianguoji-user;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 30s;
    }

    location /static/ {
        alias /data/xianguoji/upload/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location / {
        try_files \$uri \$uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }
}
NGINXEOF
rm -f /etc/nginx/sites-enabled/default
ln -sf /etc/nginx/sites-available/xianguoji-ip-demo /etc/nginx/sites-enabled/xianguoji-ip-demo
nginx -t
systemctl enable --now nginx
systemctl restart nginx

echo "部署完成：用户端 H5 http://$SERVER_IP"
echo "后端健康检查：curl http://127.0.0.1:8080/actuator/health"
