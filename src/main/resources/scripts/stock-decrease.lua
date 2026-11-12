
local stock = redis.call('GET', KEYS[1])

-- Redis 재고 키 없음
if stock == false then
    return -1
end

local quantity = tonumber(ARGV[1])
stock = tonumber(stock)

-- 잘못된 주문 수량
if quantity == nil or quantity <= 0 then
    return -2
end

-- 재고 부족
if stock < quantity then
    return -3
end

-- 원자적 재고 차감
local remainingStock = redis.call(
    'DECRBY',
    KEYS[1],
    quantity
)

-- 0도 정상 주문 결과
return remainingStock
