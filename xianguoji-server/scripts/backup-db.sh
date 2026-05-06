#!/bin/bash
# P0-7: 数据库备份脚本（crontab 调用）
# 用法: ./backup-db.sh
# 建议每天凌晨 3:00 执行: 0 3 * * * /opt/xianguoji/scripts/backup-db.sh

set -euo pipefail

BACKUP_DIR="/opt/xianguoji/backups"
RETENTION_DAYS=30
DB_NAME="xianguoji"
DB_USER="${MYSQL_USER:-root}"
DB_PASS="${MYSQL_PASSWORD}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

# mysqldump + gzip
mysqldump -u"$DB_USER" -p"$DB_PASS" --single-transaction --routines --triggers "$DB_NAME" \
    | gzip > "$FILENAME"

echo "[$(date)] Backup completed: $FILENAME ($(du -h "$FILENAME" | cut -f1))"

# 清理过期备份
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "[$(date)] Cleaned backups older than ${RETENTION_DAYS} days"
