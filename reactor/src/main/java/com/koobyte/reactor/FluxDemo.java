package com.koobyte.reactor;

import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

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

	/**
	 * Flux 和 Mono 是 Reactor 提供的最重要的组成部分，而这两个响应式类型所提供的操作就是粘合剂，这些操作将它们结合在一起，来创建数据
	 * 流动的通道。在 Flux 和 Mono 之间，存在超过 500 种操作，其中的每一个可以被归类为：
	 * <pre>
	 * 1、创建操作
	 * 2、联合操作
	 * 3、传输操作
	 * 4、逻辑处理操作
	 * </pre>
	 */
	public static void main(String[] args) {
		FluxDemo demo = new FluxDemo();

		// 创建操作
		demo.createBySimplestWay();
		demo.createAndVerify();
		demo.createByCollection();
		demo.createByInterval();

		// 联合操作
		demo.mergeFluxes();
		demo.zipFlux();
		demo.zipFluxAndHandle();

		// 传输操作

		// 逻辑操作
		demo.handleErrorAndComplete();
		demo.handleSubscribe();
		demo.disposeSubscribe();
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
		// intervalFlux.subscribe(System.out::println);
		StepVerifier.create(intervalFlux)
				.expectNext(0L)
				.expectNext(1L)
				.expectNext(2L)
				.expectNext(3L)
				.expectNext(4L)
				.verifyComplete();
	}

	public void mergeFluxes() {
		System.out.println("===== mergeFluxes =====");
		/*
		通常，Flux会迅速将数据发送给订阅者，这使得合并后的Flux无序。如果需要按照一定的顺序排列数据，可以将Flux的数据设置为延迟发送
		 */

		// 创建一个Flux，控制数据发送速度，便于测试合并后的元素顺序
		Flux<String> stringFlux = Flux
				.just("A", "B", "C")
				.delayElements(Duration.ofMillis(500)); // 延迟发送数据，每0.5s发送一个

		// 创建另一个Flux，除了控制数据发送速度，还开启延迟订阅，即：intFlux在stringFlux后执行
		Flux<String> intFlux = Flux
				.just("1", "2", "3")
				.delaySubscription(Duration.ofMillis(250)) // 延迟订阅，0.25s后才能订阅并发送数据
				.delayElements(Duration.ofMillis(500)); // 延迟发送数据

		// 合并这两个 Flux 对象后，新的合并后的 Flux 被创建。当 StepVerifier 订阅合并后的 Flux 时，它会依次订阅两个 Flux 源。
		// 合并后的 Flux 发出的数据的顺序，与源发出的数据的时间顺序一致。由于两个 Flux 都被设置为固定频率发送数据，因此值会通过合并后的 Flux 交替出现
		Flux<String> mergedFlux = stringFlux.mergeWith(intFlux);
		// 从结果可以看出，两个Flux的数据交替出现
		StepVerifier.create(mergedFlux)
				.expectNext("A")
				.expectNext("1")
				.expectNext("B")
				.expectNext("2")
				.expectNext("C")
				.expectNext("3")
				.verifyComplete();
	}

	public void zipFlux() {
		System.out.println("===== zipFlux =====");

		// 因为 mergeWith() 不能保证源之间的完美交替，所以可能需要考虑使用 zip() 操作。zip操作等待两个Flux都发出一个元素，当两个 Flux
		// 对象压缩在一起时，会产生一个新的 Flux，该 Flux 生成一个元组，其中元组包含来自每个源 Flux 的一个项
		// 与 mergeWith() 不同的是，zip() 操作是一个静态的创建操作，通过它创建的 Flux 使 character 和 food 完美对齐。从压缩后的
		// Flux 发送出来的每个项目都是 Tuple2（包含两个对象的容器），其中包含每一个源 Flux 的数据。

		Flux<String> stringFlux = Flux.just("A", "B", "C", "D");
		Flux<String> intFlux = Flux.just("1", "2", "3");
		Flux<Tuple2<String, String>> zipFlux = stringFlux.zipWith(intFlux);

		// 由于stringFlux的元素"D"在intFlux中没有与之对应的元素，所以会被丢弃

		StepVerifier.create(zipFlux)
				.expectNextMatches(t -> t.getT1().equals("A") && t.getT2().equals("1"))
				.expectNextMatches(t -> t.getT1().equals("B") && t.getT2().equals("2"))
				.expectNextMatches(t -> t.getT1().equals("C") && t.getT2().equals("3"))
				.verifyComplete();
	}

	public void zipFluxAndHandle() {
		System.out.println("===== zipFluxAndHandle =====");

		Flux<String> stringFlux = Flux.just("A", "B", "C", "D");
		Flux<String> intFlux = Flux.just("1", "2", "3");
		// 合并Flux并提供处理函数
		Flux<String> zipFlux = stringFlux.zipWith(intFlux, (s, i) -> "s" + "->" + i);
		// 也可以使用下边的方式，效果相同
		// Flux<String> zipFlux = Flux.zip(stringFlux, intFlux, (s, i) -> s + "->" + i);
		StepVerifier.create(zipFlux)
				.expectNext("A->1")
				.expectNext("B->2")
				.expectNext("C->3")
				.verifyComplete();
	}

	public void handleErrorAndComplete() {
		System.out.println("===== handleErrorAndComplete =====");
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
	}

	public void handleSubscribe() {
		// 创建一个Flux
		Flux<Integer> ints = Flux.range(1, 4);
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

	public void disposeSubscribe() {
		System.out.println("===== disposeSubscribe =====");

		Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)).take(100);
		Disposable disposable = intervalFlux.subscribe(System.out::println);

	}
}