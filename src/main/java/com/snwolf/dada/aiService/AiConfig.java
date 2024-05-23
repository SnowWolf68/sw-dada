package com.snwolf.dada.aiService;

import com.snwolf.dada.properties.ZhipuProperties;
import com.zhipu.oapi.ClientV4;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConfig {

    private final ZhipuProperties zhipuProperties;

    @Bean
    public ClientV4 getClientV4() {
        return new ClientV4.Builder(zhipuProperties.getApiKey()).build();
    }
}
