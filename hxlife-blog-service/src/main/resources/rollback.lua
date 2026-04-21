
local voucherIdStockKey=ARGV[1]
local orderKey=ARGV[2]
local userIdKey=ARGV[3]

--增加库存
redis.call("incrby",voucherIdStockKey,1)
--删除订单
redis.call("srem",orderKey,userIdKey)

return 1


