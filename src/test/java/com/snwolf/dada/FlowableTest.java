package com.snwolf.dada;

import com.snwolf.dada.aiService.ZhipuAiServiceImpl;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

//@SpringBootTest
@Slf4j
@RequiredArgsConstructor
public class FlowableTest {

//    private final ZhipuAiServiceImpl zhipuAiService;

    @Test
    void testFlowable() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        Flowable<Integer> flowable = Flowable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onNext(4);
            emitter.onNext(5);
            emitter.onNext(6);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
        AtomicInteger count = new AtomicInteger(0);
        flowable.observeOn(Schedulers.io())
                .map(o -> o)
                .flatMap(o -> Flowable.just(o))
                .doOnNext(o -> {
                    count.incrementAndGet();
                    System.out.println("o = " + o);
                    countDownLatch.countDown();
                })
                .subscribe();
        countDownLatch.await();
        System.out.println("count = " + count);
    }
}
