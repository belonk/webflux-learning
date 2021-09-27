package com.koobyte.reactor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * Created by sun on 2021/9/27.
 *
 * @author sunfuchang03@126.com
 * @since 3.0
 */
public class SubscribeDemo {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods

	public static void main(String[] args) {
		SubscribeDemo demo = new SubscribeDemo();
		demo.handleSubscribe();
	}

	public void handleSubscribe() {
		// 创建一个Flux
		Flux<Integer> ints;

		// subscribe方法已经过期，不推荐使用了
		// ints = Flux.range(1, 4);
		// 订阅并设置收到订阅信号的回调，过期的方法
		// ints.subscribe(System.out::println,
		// 		error -> System.err.println("Error " + error),
		// 		() -> System.out.println("Done"),
		// 		// 订阅回调(onSubscribe被调用)，设置可以接收的数据个数, 如果个数小于发送者发送的个数，
		// 		// 表示流为处理完成，不会执行完成回调；相反，超过发送的个数认为流处理完成，可以执行完成回调
		// 		sub -> sub.request(2));

		// 推荐使用subscribeWith方法，效果同上边过期的方法
		ints = Flux.range(1, 4);
		ints.subscribeWith(new BaseSubscriber<Integer>() {
			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				subscription.request(2);
			}

			@Override
			protected void hookOnNext(Integer value) {
				// 处理
				System.out.println(value);
				// 处理完成后再请求一个数据，这里可以根据情况设置请求数量来控制背压
				request(1);
			}

			@Override
			protected void hookOnError(Throwable throwable) {
				System.err.println("Error " + throwable);
			}

			@Override
			protected void hookOnComplete() {
				System.out.println("Done");
			}
		});
	}
}