
local stock = redis.call('GET', KEYS[1])

if stock == false then
    return -1
end

local quantity = tonumber(ARGV[1])

if quantity == nil or quantity <= 0 then
    return -2
end

local restoredStock =
    redis.call('INCRBY', KEYS[1], quantity)

return restoredStock
