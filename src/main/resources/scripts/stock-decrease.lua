
local stock = redis.call('GET', KEYS[1])

-- 재고 키 없음
if stock == false then
    return -1
end

local quantity = tonumber(ARGV[1])
stock = tonumber(stock)

-- 잘못된 수량
if quantity == nil or quantity <= 0 then
    return -2
end

-- 재고 부족
if stock < quantity then
    return 0
end

-- 원자적 재고 차감
local remainingStock =
    redis.call('DECRBY', KEYS[1], quantity)

return remainingStock
