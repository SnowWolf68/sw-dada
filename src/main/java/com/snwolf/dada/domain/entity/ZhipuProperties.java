package com.snwolf.dada.domain.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "sw.zhipu")
@Component
@Data
@Slf4j
public class ZhipuProperties {
    private String apiKey;

    @PostConstruct
    private void postConstruct(){
        log.info("apiKey: {}", apiKey);
    }
}
