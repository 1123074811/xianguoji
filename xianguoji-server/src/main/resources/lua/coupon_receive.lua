-- KEYS[1] = xgj:coupon:received:{couponId}   (total received count)
-- KEYS[2] = xgj:coupon:user:{couponId}:{uid}  (per-user received count)
-- ARGV[1] = total limit (0=unlimited)
-- ARGV[2] = per-user limit
-- Returns: 1=success, 0=total exceeded, -1=per-user exceeded

-- Check total limit
local total = tonumber(redis.call('get', KEYS[1]))
if not total then total = 0 end
if tonumber(ARGV[1]) > 0 and total >= tonumber(ARGV[1]) then return 0 end

-- Check per-user limit
local userCount = tonumber(redis.call('get', KEYS[2]))
if not userCount then userCount = 0 end
if userCount >= tonumber(ARGV[2]) then return -1 end

-- Both checks passed, increment
redis.call('incr', KEYS[1])
redis.call('incr', KEYS[2])
return 1
