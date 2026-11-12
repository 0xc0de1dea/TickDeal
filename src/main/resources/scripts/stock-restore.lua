
-- KEYS[1] = 재고 키
-- KEYS[2] = 주문별 재고 복구 키
-- ARGV[1] = 복구 수량

-- 이미 복구한 주문이면 중복 복구 방지
if redis.call('EXISTS', KEYS[2]) == 1 then
    return 0
end

local quantity = tonumber(ARGV[1])

if quantity == nil or quantity <= 0 then
    return -2
end

-- Redis 재고 복구
local restoredStock = redis.call(
    'INCRBY',
    KEYS[1],
    quantity
)

-- 복구 완료 기록
redis.call(
    'SET',
    KEYS[2],
    '1'
)

return restoredStock
