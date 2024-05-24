package com.snwolf.dada.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Slf4j
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private Integer database;

    private String password;

    @Bean
    public RedissonClient redissonClient(){
        log.info("RedissonClient init");
        log.info("host:{},port:{},database:{},password:{}",host,port,database,password);

        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password);
        return Redisson.create(config);
    }
}
