package com.koobyte.reactor;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2021/9/27.
 *
 * @author sunfuchang03@126.com
 * @since 3.0
 */
public class DisposableDemo {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods

	public static void main(String[] args) {
		DisposableDemo demo = new DisposableDemo();
		demo.disposeSubscribe();
	}

	public void disposeSubscribe() {
		System.out.println("> disposeSubscribe :");

		Flux<Integer> integerFlux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));
		/*
		 * 执行subscribe后，控制权直接返回调用线程，随着主线的退出，这里的订阅逻辑看起来不会执行，需要让主线程等待一段时间
		 */
		Disposable disposable = integerFlux.subscribe(System.out::println);

		// 开启定时器，一段时间后取消订阅。取消后，可以看到订阅逻辑不再有效
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Try to dispose.");
				disposable.dispose();
				timer.cancel();
			}
		}, 3500);

		try {
			TimeUnit.SECONDS.sleep(6);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}