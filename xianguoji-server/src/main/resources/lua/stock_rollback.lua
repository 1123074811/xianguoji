-- KEYS[1] = stock:sku:{skuId}
-- ARGV[1] = quantity to rollback
-- 仅当 key 存在时才回滚；否则避免误创建假库存
if redis.call('exists', KEYS[1]) == 1 then
  redis.call('incrby', KEYS[1], ARGV[1])
  return 1
end
return 0
