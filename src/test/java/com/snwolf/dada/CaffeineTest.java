package com.snwolf.dada;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CaffeineTest {

    @Test
    void test(){
        Cache<String, String> cache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
        cache.put("zhangsan", "123");
        String res = cache.getIfPresent("zhangsan");
        log.info("res:{}", res);
    }
}
