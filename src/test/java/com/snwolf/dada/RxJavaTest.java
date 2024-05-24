package com.snwolf.dada;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

@Slf4j
public class RxJavaTest {

    @Test
    public void test1() throws InterruptedException {
        // 创建被观察者
        Observable<Object> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onError(new Exception("故意的"));
            emitter.onComplete();
        });

//        Observable.create(new ObservableOnSubscribe<Object>() {
//            @Override
//            public void subscribe(@NotNull ObservableEmitter<Object> emitter) throws Exception {
//
//            }
//        })

        // 创建观察者
        Observer<Object> observer = new Observer<Object>() {

            @Override
            public void onSubscribe(@NotNull Disposable d) {
                log.info("onSubscribe调用");
            }

            @Override
            public void onNext(@NotNull Object o) {
                log.info("onNext调用");
                log.info("事件: o{}", o);
            }

            @Override
            public void onError(@NotNull Throwable e) {
                log.info("onError调用");
                log.error("error: e{}", e);
            }

            @Override
            public void onComplete() {
                log.info("onComplete调用");
            }
        };

        // 被观察者订阅观察者
        observable.subscribe(observer);

        Thread.sleep(10000L);
    }

    @Test
    void test2() throws InterruptedException {
        Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onError(new Exception("故意的"));
            emitter.onComplete();
        }).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                log.info("onSubscribe调用");
            }

            @Override
            public void onNext(@NotNull Object o) {
                log.info("onNext调用");
                log.info("事件, o:{}", o);
            }

            @Override
            public void onError(@NotNull Throwable e) {
                log.info("onError调用");
                log.error("error, e:{}", e);
            }

            @Override
            public void onComplete() {
                log.info("onComplete调用");
            }
        });

        Thread.sleep(5000L);
    }

}
