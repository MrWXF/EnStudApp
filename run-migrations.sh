#!/bin/bash
# 执行所有数据库迁移脚本

cd /d/project/github/EnStudApp

echo "=== 执行数据库迁移 ==="

for file in sql/migrations/V{4,5,6,7,8}*.sql; do
  echo "Executing $file..."
  docker exec -i enstud-mysql mysql -uroot -proot enstud_app < "$file" 2>&1
  if [ $? -eq 0 ]; then
    echo "✓ Success"
  else
    echo "✗ Failed"
  fi
  echo ""
done

echo "=== 验证表是否创建 ==="
docker exec -it enstud-mysql mysql -uroot -proot enstud_app -e "SHOW TABLES LIKE 'enstud_%';"
