# O-1~8: 运维部署指南

## O-1: Nginx 反向代理配置

```nginx
server {
    listen 443 ssl http2;
    server_name api.xianguoji.com;

    ssl_certificate     /etc/nginx/ssl/api.xianguoji.com.pem;
    ssl_certificate_key /etc/nginx/ssl/api.xianguoji.com.key;
    ssl_protocols       TLSv1.2 TLSv1.3;

    # 安全头
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # API 反向代理
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 30s;
    }

    # 静态资源（上传文件）
    location /static/ {
        alias /opt/xianguoji/upload/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 健康检查
    location /actuator/health {
        proxy_pass http://127.0.0.1:8080;
        allow 10.0.0.0/8;
        deny all;
    }
}
```

## O-2: SSL/TLS 配置

- 证书: Let's Encrypt 或阿里云免费证书
- 协议: TLSv1.2 + TLSv1.3
- 密码套件: ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256
- HSTS: max-age=31536000; includeSubDomains

## O-3: Docker Compose 部署

```yaml
version: "3.8"
services:
  app:
    build: ./xianguoji-server
    ports:
      - "8080:8080"
    env_file: .env
    depends_on:
      - redis
      - mysql
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_DATABASE: xianguoji
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
    restart: unless-stopped
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
    volumes:
      - redis_data:/data
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
```

## O-4: 健康检查端点

Spring Boot Actuator 已引入，需在 application-prod.yml 暴露 health 端点:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

## O-5: 日志采集

- 应用日志: logback 滚动到 `/opt/xianguoji/logs/`
- Nginx 日志: `/var/log/nginx/`
- 推荐: Filebeat → Elasticsearch → Kibana

## O-6: 监控告警

- Prometheus + Grafana 监控 JVM / API 延迟 / Redis
- 告警规则: 5xx > 1%/min, P99 > 3s, Redis 内存 > 80%
- 安全告警: 通过 SecurityEventAlerter 推送钉钉 webhook

## O-7: 灰度发布

- Nginx upstream 分组: stable / canary
- 按比例分流: `split_clients "${remote_addr}" $variant { 90% stable; * canary; }`
- 金丝雀观察 10 分钟后全量

## O-8: 应急预案

| 故障场景 | 检测方式 | 应急动作 |
|---------|---------|---------|
| API 5xx 飙升 | Prometheus 告警 | 回滚上一镜像版本 |
| Redis 宕机 | 健康检查失败 | 切换哨兵从节点，降级缓存 |
| MySQL 不可用 | 连接池告警 | 启动只读模式，切换从库 |
| 磁盘满 | 磁盘使用率 > 90% | 清理过期日志/备份 |
| DDoS | 请求量异常 | Nginx limit_req + WAF |
