
local stock = redis.call('GET', KEYS[1])

-- 재고 데이터가 없는 경우
if stock == false then
    return -1
end

-- 재고 복구
local restoredStock = redis.call('INCR', KEYS[1])

return restoredStock
