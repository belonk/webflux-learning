package com.koobyte.reactor;

import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <code>Flux</code>表示包含0个或多个数据的流。
 * <code>Mono</code>表示包含0个或1个数据的流。
 *
 * @author sunfuchang03@126.com
 * @since 3.0
 */
public class BasicOperationDemo {
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
		BasicOperationDemo demo = new BasicOperationDemo();

		// 创建操作
		System.out.println("===== 创建操作 =====");
		demo.createBySimplestWay();
		demo.createAndVerify();
		demo.createByCollection();
		demo.createByInterval();

		// 联合操作
		System.out.println("===== 联合操作 =====");
		demo.mergeFluxes();
		demo.zipFlux();
		demo.zipFluxAndHandle();
		demo.firstFlux();

		// 传输操作
		System.out.println("===== 传输操作 =====");
		demo.skipItems();
		demo.skipByTime();
		demo.takeItems();
		demo.takeByTime();
		demo.filter();
		demo.distinct();
		demo.map();
		demo.flatMap();
		demo.buffer();
		demo.bufferThenParallel();
		demo.collectList();
		demo.collectMap();

		// 逻辑操作
		System.out.println("===== 逻辑操作 =====");
		demo.all();
		demo.any();
	}

	// ===========
	// 创建
	// ===========

	public void createBySimplestWay() {
		System.out.println("> createBySimplestWay :");
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
		System.out.println("> createAndVerify :");
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
		System.out.println("> createByCollection :");

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
		System.out.println("> createByInterval :");

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

	// ===========
	// 联合
	// ===========

	public void mergeFluxes() {
		System.out.println("> mergeFluxes :");
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
		System.out.println("> zipFlux :");

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
		System.out.println("> zipFluxAndHandle :");

		Flux<String> stringFlux = Flux.just("A", "B", "C", "D");
		Flux<String> intFlux = Flux.just("1", "2", "3");
		// 合并Flux并提供处理函数
		Flux<String> zipFlux = stringFlux.zipWith(intFlux, (s, i) -> s + "->" + i);
		// 也可以使用下边的方式，效果相同
		// Flux<String> zipFlux = Flux.zip(stringFlux, intFlux, (s, i) -> s + "->" + i);
		StepVerifier.create(zipFlux)
				.expectNext("A->1")
				.expectNext("B->2")
				.expectNext("C->3")
				.verifyComplete();
	}

	public void firstFlux() {
		System.out.println("> firstFlux :");

		// 创建延迟发送数据的Flux
		Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth").delaySubscription(Duration.ofMillis(100));
		// 创建普通Flux
		Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");

		// 创建一个新的Flux，该Flux会收集最快发送数据的那个Flux的数据
		Flux<String> firstFlux = Flux.firstWithSignal(slowFlux, fastFlux);
		// 3.4版本被废弃，3.5移除
		// Flux<String> firstFlux = Flux.first(slowFlux, fastFlux);

		StepVerifier.create(firstFlux)
				.expectNext("hare")
				.expectNext("cheetah")
				.expectNext("squirrel")
				.verifyComplete();
	}

	// ===========
	// 传输
	// ===========

	public void skipItems() {
		System.out.println("> skipItems :");

		Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5, 6);

		// skip操作会跳过指定数量的元素，Flux只会发布后3个元素
		Flux<Integer> skippedFlux = integerFlux.skip(3);
		StepVerifier.create(skippedFlux)
				.expectNext(4, 5, 6)
				.verifyComplete();
	}

	public void skipByTime() {
		System.out.println("> skipByTime :");

		// 创建一个每秒发送一个元素的Flux
		Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5, 6).delayElements(Duration.ofSeconds(1));
		// 生成一个新的FLux，跳过前3秒发送的元素，不包含元素3
		Flux<Integer> skip = integerFlux.skip(Duration.ofSeconds(3));

		// skip包含第3秒发送的元素3
		StepVerifier.create(skip)
				.expectNext(3, 4, 5, 6) // 会发送元素3
				.verifyComplete();
	}

	public void takeItems() {
		System.out.println("> takeItems :");

		Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5, 6);
		// take可以看成skip的反向操作，它只会接收前边发送的几个元素
		Flux<Integer> skip = integerFlux.take(3);

		StepVerifier.create(skip).expectNext(1, 2, 3).verifyComplete();
	}

	public void takeByTime() {
		System.out.println("> takeByTime :");

		Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5, 6).delayElements(Duration.ofSeconds(1));
		Flux<Integer> skip = integerFlux.take(Duration.ofSeconds(3));
		// take操作不会不会包含第三秒发送的元素3
		StepVerifier.create(skip).expectNext(1, 2).verifyComplete();
	}

	public void filter() {
		System.out.println("> filter :");

		// filter是更通用的元素过滤方法，指定一个条件：Predicate过滤函数
		// filter理解为保留，即：会保留符合条件的项(Predicate函数返回true)，不符合条件的会过滤掉
		Flux<String> stringFlux = Flux.just(
				"hello", "world", "hello world", "web flux", "web")
				.filter(np -> !np.contains(" "));

		StepVerifier.create(stringFlux)
				.expectNext("hello", "world", "web")
				.verifyComplete();
	}

	public void distinct() {
		System.out.println("> distinct :");

		// distinct: 去除重复的元素
		Flux<String> stringFlux = Flux.just(
				"hello", "world", "hello", "web", "web")
				.distinct();

		StepVerifier.create(stringFlux)
				.expectNext("hello", "world", "web")
				.verifyComplete();
	}

	// 对于 Flux 或 Mono，最常用的操作之一是将已发布的项转换为其他形式或类型。Reactor 为此提供 map() 和flatMap() 操作。

	public void map() {
		System.out.println("> map :");

		// 创建一个Flux
		Flux<String> stringFlux = Flux.just("1", "2", "3");
		// map: 处理flux的每一个元素，返回一个新的Flux
		// map映射是同步执行的，因为每个项都是由源 Flux 发布的。如果要异步执行映射，应考虑使用 flatMap() 操作。
		Flux<Integer> integerFlux = stringFlux.map(Integer::parseInt);

		StepVerifier.create(integerFlux)
				.expectNext(1, 2, 3).verifyComplete();
	}

	public void flatMap() {
		System.out.println("> flatMap :");

		// flatMap() 不是简单地将一个对象映射到另一个对象，而是将每个对象映射到一个新的 Mono 或 Flux。Mono 或 Flux 的结果被压成一个
		// 新的 Flux。当与subscribeOn() 一起使用时，flatMap() 可以使用subscribeOn()释放 Reactor 类型的异步能力。
		//
		// subscribeOn()支持的策略：
		// 1、.immediate()在当前线程中执行订阅
		// 2、.single()在单个可重用线程中执行订阅，对所有调用方重复使用同一线程
		// 3、.newSingle()在每个调用专用线程中执行订阅
		// 4、.elastic()在从无限弹性池中提取的工作进程中执行订阅，根据需要创建新的工作线程，并释放空闲的工作线程（默认情况下 60 秒）
		// 5、.parallel()在从固定大小的池中提取的工作进程中执行订阅，该池的大小取决于 CPU 核心的数量。

		// 创建一个Flux
		Flux<String> stringFlux = Flux.just("1", "2", "3");
		// flatMap将元素转换为一个Mono或Flux，而不是单纯的整型
		Flux<Integer> integerFlux = stringFlux.flatMap(new Function<String, Mono<Integer>>() {
			@Override
			public Mono<Integer> apply(String s) {
				return Mono.just(Integer.parseInt(s));
			}
		}).subscribeOn(Schedulers.parallel()); // 调用 subscribeOn() 来指定每个订阅应该在一个并行线程池中进行，并行执行
		// 简写
		// Flux<Integer> integerFlux = stringFlux.flatMap((Function<String, Mono<Integer>>) s -> Mono.just(Integer.parseInt(s)));

		StepVerifier.create(integerFlux).expectNext(1, 2, 3).verifyComplete();
	}

	public void buffer() {
		System.out.println("> buffer :");

		// 创建Flux
		Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");

		// 使用buffer方法将Flux流分块, 新创建一个List集合的块，每块最多包含3个元素
		Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);
		StepVerifier.create(bufferedFlux)
				.expectNext(Arrays.asList("apple", "orange", "banana"))
				.expectNext(Arrays.asList("kiwi", "strawberry"))
				.verifyComplete();
	}

	public void bufferThenParallel() {
		System.out.println("> bufferThenParallel :");

		// 创建Flux
		Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
		fruitFlux.buffer(3)
				.flatMap(list -> Flux.fromIterable(list) // 将list转为Flux
								.map(String::toUpperCase) // 使用map将水果名称转为大写
								.subscribeOn(Schedulers.parallel()) // 并行发送
						// .log() // 打印日志，可以通过日志跟踪流数据转换情况，这里可以看到，所有水果名称的转换在2个线程中完成，线程名为parallel-x
				).subscribe();
	}

	public void collectList() {
		System.out.println("> collectList :");

		Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
		// 调用collectList将所有元素转为List，结果返回一个Mono，该效果与不带参数的buffer()方法相同
		Mono<List<String>> fruitListMono = fruitFlux.collectList();

		StepVerifier.create(fruitListMono)
				.expectNext(Arrays.asList("apple", "orange", "banana", "kiwi", "strawberry"))
				.verifyComplete();
	}

	public void collectMap() {
		System.out.println("> collectMap :");

		Flux<String> stringFlux = Flux.just("hello", "flux", "java");
		// 将flux按照给定参数函数转为map，参数可以指定如何生成key和value
		Mono<Map<String, String>> mapMono = stringFlux.collectMap(s -> s.substring(0, 1)); // 第一个字母作为key，原始元素作为value

		StepVerifier.create(mapMono)
				.expectNextMatches(map -> map.size() == 3
						&& map.get("h").equals("hello")
						&& map.get("f").equals("flux")
						&& map.get("j").equals("java"))
				.verifyComplete();
	}

	// ===========
	// 逻辑
	// ===========

	public void all() {
		System.out.println("> all :");

		// 创建Flux
		Flux<String> stringFlux = Flux.just("apple", "xiaomi", "huawei");
		// all: 所有元素都匹配给定条件函数
		// 这里是所有元素都包含a，才会返送true
		Mono<Boolean> all = stringFlux.all(m -> m.contains("a"));
		StepVerifier.create(all).expectNext(true).verifyComplete();

		// 所有元素都必须以a开头，显然是false
		all = stringFlux.all(m -> m.startsWith("a"));
		StepVerifier.create(all).expectNext(false).verifyComplete();
	}

	public void any() {
		System.out.println("> any :");

		// any: 只要有一个元素符合条件，则会发送true
		Flux<String> stringFlux = Flux.just("apple", "xiaomi", "huawei");
		// 任何一个元素包含字母p，则会发送true
		Mono<Boolean> any = stringFlux.any(m -> m.contains("p"));
		StepVerifier.create(any).expectNext(true).verifyComplete();

		// 任何一个元素以字母o开头，则发送true，否则发送false
		any = stringFlux.any(m -> m.startsWith("o"));
		StepVerifier.create(any).expectNext(false).verifyComplete();
	}
}