-- KEYS[1] = stock:sku:{skuId}
-- ARGV[1] = quantity to deduct
-- Returns: 1 = success, 0 = insufficient stock
local stock = tonumber(redis.call('get', KEYS[1]))
if not stock or stock < tonumber(ARGV[1]) then return 0 end
redis.call('decrby', KEYS[1], ARGV[1])
return 1
