package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.services.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ERROR_CONVERTING_MSG = "Error while converting JSON";

    @Override
    public <T> T getValue(String key, Class<T> valueType) {
        T t = null;
        try {
            String dataJson = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(dataJson)) {
                t = objectMapper.readValue(dataJson, valueType);
            }
        } catch (JsonProcessingException e) {
            log.error(ERROR_CONVERTING_MSG, e);
        }
        return t;
    }

    @Override
    public <T> T getValue(String key, TypeReference<T> typeReference) {
        T t = null;
        try {
            String dataJson = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(dataJson)) {
                t = objectMapper.readValue(dataJson, typeReference);
            }
        } catch (JsonProcessingException e) {
            log.error(ERROR_CONVERTING_MSG, e);
        }
        return t;
    }

    @Override
    public void deleteByKey(@NotNull String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByKeys(@NotNull String... patternsOrKeys) {
        for (String pattern : patternsOrKeys) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }


    @Override
    public <T> void setValue(String key, T data) {
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, dataJson);
        } catch (JsonProcessingException e) {
            log.error(ERROR_CONVERTING_MSG, e);
        }
    }

    @Override
    public <T> void setValue(String key, T data, int expireDurationSeconds) {
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, dataJson, expireDurationSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error(ERROR_CONVERTING_MSG, e);
        }
    }
}