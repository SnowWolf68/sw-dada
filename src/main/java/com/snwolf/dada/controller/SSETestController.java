package com.snwolf.dada.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SSETestController {

    @GetMapping("/test")
    public SseEmitter test(String name) {
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    String message = "This is message " + i + " name: " + name;
                    emitter.send(message);
                    log.info("send message: {}", message);
                    Thread.sleep(1000L);
                }
                emitter.complete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        return emitter;
    }
}
