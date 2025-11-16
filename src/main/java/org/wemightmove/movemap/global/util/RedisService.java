package org.wemightmove.movemap.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setValuesWithTimeout(String key, String value, Duration timeout){
        redisTemplate.opsForValue().set(key,value,timeout);
    }

    //키가 없을 때만 값을 저장하고 true 반환, 키가 이미 있으면 저장하지 않고 false 반환
    public boolean setValuesWithTimeoutIfAbsent(String key, String value, Duration timeout){
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout);
    }

    @Transactional(readOnly = true)
    public Object getValues(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValues(String key){
        redisTemplate.delete(key);
    }

}
