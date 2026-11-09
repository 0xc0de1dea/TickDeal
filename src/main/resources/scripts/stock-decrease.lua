
local stock = redis.call('GET', KEYS[1])

-- 재고 데이터가 존재하지 않는 경우
if stock == false then
    return -1
end

stock = tonumber(stock)

-- 재고 부족
if stock <= 0 then
    return 0
end

-- 재고 차감
local remainingStock = redis.call('DECR', KEYS[1])

-- 차감 후 남은 재고 반환
return remainingStock
