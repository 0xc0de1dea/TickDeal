package com.example.tickdeal.domain.inventory.service;

import com.example.tickdeal.common.exception.ErrorCode;
import com.example.tickdeal.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import static com.example.tickdeal.common.constant.Constants.STOCK_KEY_PREFIX;
import static com.example.tickdeal.common.constant.Constants.STOCK_KEY_SUFFIX;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RedisTemplate<String, String> redisTemplate;

    private final DefaultRedisScript<Long> decreaseStockScript =
            createScript("scripts/stock-decrease.lua");

    private final DefaultRedisScript<Long> restoreStockScript =
            createScript("scripts/stock-restore.lua");

    private DefaultRedisScript<Long> createScript(String path) {
        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();

        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);

        return script;
    }

    private String getStockKey(Long dealId) {
        return STOCK_KEY_PREFIX +
                dealId +
                STOCK_KEY_SUFFIX;
    }

    public void initializeStock(Long dealId, Integer stock) {
        String key = getStockKey(dealId);

//        redisTemplate.opsForValue().set(
//                key,
//                String.valueOf(stock)
//        );

        Boolean initialized = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(stock));

        if (!Boolean.TRUE.equals(initialized)) {
            throw new ServiceException(
                    ErrorCode.STOCK_ALREADY_INITIATED_REDIS
            );
        }
    }

    public Long getStock(Long dealId) {
        String key = getStockKey(dealId);

        String stock = redisTemplate.opsForValue().get(key);

        if (stock == null) {
            throw new ServiceException(
                    ErrorCode.STOCK_INFORMATION_NOT_EXISTS_REDIS
            );
        }

        return Long.parseLong(stock);
    }

    public Long decreaseStock(Long dealId) {
        String key = getStockKey(dealId);

        Long result = redisTemplate.execute(
                decreaseStockScript,
                java.util.Collections.singletonList(key)
        );

        if (result == null) {
            throw new ServiceException(
                    ErrorCode.STOCK_DECREASE_RESULT_NOT_EXISTS_REDIS
            );
        }

        return result;
    }

    public Long restoreStock(Long dealId) {
        String key = getStockKey(dealId);

        Long result = redisTemplate.execute(
                restoreStockScript,
                java.util.Collections.singletonList(key)
        );

        if (result == null) {
            throw new ServiceException(
                    ErrorCode.STOCK_RESTORE_RESULT_NOT_EXISTS_REDIS
            );
        }

        return result;
    }

    public boolean existsStock(Long dealId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(getStockKey(dealId))
        );
    }
}
