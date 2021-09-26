package com.koobyte.reactor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * <code>Flux</code>表示包含0个或多个数据的流。
 * <code>Mono</code>表示包含0个或1个数据的流。
 *
 * @author sunfuchang03@126.com
 * @since 3.0
 */
public class FluxDemo {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods

	public static void main(String[] args) {
		FluxDemo demo = new FluxDemo();
		demo.createBySimplestWay();
		demo.createAndVerify();
		demo.createByCollection();
		demo.createByInterval();
		demo.createWithErrorAndComplete();
	}

	public void createBySimplestWay() {
		System.out.println("===== createBySimplestWay =====");
		// 创建一个Flux
		Flux<String> seq1 = Flux.just("foo", "bar", "foobar");
		// 订阅后，数据开始流动，但是没有任何输出
		// seq1.subscribe();
		// 订阅并提供一个数据操作的回调
		seq1.subscribe(System.out::println);

		// 创建空Mono
		Mono<String> noData = Mono.empty();
		noData.subscribe(System.out::println);
		// 创建单个元素的mono
		Mono<String> data = Mono.just("foo");
		data.subscribe(System.out::println);

		// 创建整数序列
		Flux<Integer> numbersFromFiveToSeven = Flux.range(5, 3);
		numbersFromFiveToSeven.subscribe(System.out::println);
	}

	public void createAndVerify() {
		System.out.println("===== createAndVerify =====");
		// 创建水果序列
		Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
		// 打印输出
		// fruitFlux.subscribe(f -> System.out.println("Here's some fruit: " + f));
		// 使用reactor-test进行结果预期验证
		StepVerifier.create(fruitFlux)
				.expectNext("Apple") // 期望下一个发送的数据
				.expectNext("Orange")
				.expectNext("Grape")
				.expectNext("Banana")
				.expectNext("Strawberry")
				// .expectNext("Error") // 故意设置异常
				.verifyComplete(); // 验证，检查是否收到完成信号，如果有异常则打印异常信息

	}

	public void createByCollection() {
		System.out.println("===== createByCollection =====");

		// 从数组创建Flux
		String[] array = new String[]{"foo", "bar", "foobar"};
		Flux<String> seq1 = Flux.fromArray(array);
		StepVerifier.create(seq1).expectNext("foo").expectNext("bar").expectNext("foobar").verifyComplete();

		// 从集合创建Flux
		List<String> iterable = Arrays.asList("foo", "bar", "foobar");
		Flux<String> seq2 = Flux.fromIterable(iterable);
		StepVerifier.create(seq2).expectNext("foo").expectNext("bar").expectNext("foobar").verifyComplete();

		// 从Stream创建Flux
		Stream<String> fruitStream =
				Stream.of("foo", "bar", "foobar");
		Flux<String> seq3 = Flux.fromStream(fruitStream);
		StepVerifier.create(seq3).expectNext("foo").expectNext("bar").expectNext("foobar").verifyComplete();
	}

	public void createByInterval() {
		System.out.println("===== createByInterval =====");

		// 创建定时发送数据的Flux，从0开始，依次递增，可以无限发送，可以使用take操作限定获取前5个数据
		Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)).take(5);
		intervalFlux.subscribe(System.out::println);
		// StepVerifier.create(intervalFlux)
		// 		.expectNext(0L)
		// 		.expectNext(1L)
		// 		.expectNext(2L)
		// 		.expectNext(3L)
		// 		.expectNext(4L)
		// 		.verifyComplete();
	}

	public void createWithErrorAndComplete() {
		System.out.println("===== createWithErrorAndComplete =====");
		// 创建整数序列的Flux，从1开始，共4个元素
		Flux<Integer> ints = Flux.range(1, 4)
				// 处理每个元素，如果i<=3正常返回，否则抛出异常
				.map(i -> {
					if (i <= 3) return i;
					throw new RuntimeException("Got to 4");
				});
		// 订阅Flux，并且传递错误处理回调函数，打印错误信息
		ints.subscribe(System.out::println, error -> System.err.println("Error: " + error));

		/*
		 * 错误和完成两者不会同时执行，存在错误则执行错误回调，否则执行完成回调。执行错误或者完成流都会终止
		 */

		// 重新创建一个Flux
		ints = Flux.range(1, 4);
		// 订阅，并传递错误处理函数和执行完成函数
		ints.subscribe(System.out::println,
				error -> System.err.println("Error " + error),
				() -> System.out.println("Done"));

		// 重新创建一个Flux
		ints = Flux.range(1, 4);
		// 订阅并设置收到订阅信号的回调，过期的方法
		ints.subscribe(System.out::println,
				error -> System.err.println("Error " + error),
				() -> System.out.println("Done"),
				// 订阅回调(onSubscribe被调用)，设置可以接收的数据个数, 如果个数小于发送者发送的个数，
				// 表示流为处理完成，不会执行完成回调；相反，超过发送的个数认为流处理完成，可以执行完成回调
				sub -> sub.request(2));
		// 推荐使用subscribeWith方法，效果同上边过期的方法
		ints = Flux.range(1, 4);
		ints.subscribeWith(new BaseSubscriber<Integer>() {
			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				subscription.request(2);
			}

			@Override
			protected void hookOnNext(Integer value) {
				System.out.println(value);
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