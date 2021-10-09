package com.koobyte.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2021/9/29.
 *
 * @author sunfuchang03@126.com
 * @since 3.0
 */
public class ScheduleDemo {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods

	/**
	 * 在 Reactor 中，执行模型和执行发生的位置取决于 Scheduler 所使用的模型，Scheduler类似于ExecuteService，它是一个执行模型抽象。
	 * <p>
	 * {@link reactor.core.scheduler.Schedulers}类提供了创建{@link reactor.core.scheduler.Scheduler}的静态方法，包括：
	 * <li>{@link Schedulers#immediate()}: 在当前线程中执行订阅，可以看做是创建空的Scheduler
	 * <li>{@link Schedulers#single()}: 在单个可重用线程中执行订阅，对所有调用方重复使用同一线程, 如果想要每一个方法在不同线程中执行，
	 * 可以使用{@link Schedulers#newSingle(String)}
	 * <li>{@link Schedulers#elastic()}: 在从无限弹性池中提取的工作进程中执行订阅，根据需要创建新的工作线程，并释放空闲的工作线程
	 * （默认情况下 60 秒）, 已经被弃用，因为它会创建太多线程，而且会隐藏背压问题
	 * <li>{@link Schedulers#boundedElastic()}: 使用有界弹性线程池，它按需创建工作线程，并重用空闲的线程，空闲的线程存活时间为60秒。
	 * 线程池有容量上限，默认是CPU核心数 x 10个，提交的任务到达100000个上限后就会排队，并在线程可用时重新调度
	 * <li>{@link Schedulers#parallel()}: 针对并行工作进行调整的固定工作线程池，它会创建与 CPU 内核一样多的工作线程
	 * <p>
	 * 您可以通过{@link Schedulers#fromExecutorService(ExecutorService)}重用已存在的ExecutorService来创建Scheduler，但不鼓励这样做。
	 * <p>
	 * 您还可以使用这些newXXX 方法创建各种调度程序类型的新实例。例如，使用Schedulers.publicOnNewParallel(yourScheduleName)创建一个名为
	 * yourScheduleName 的新并行调度程序。
	 */
	public static void main(String[] args) throws Exception {
		ScheduleDemo demo = new ScheduleDemo();
		demo.exeInNewThread();
		// TODO 看起来都是在单个线程中执行的，如何并行执行？
		demo.scheduleImmediate();
		demo.scheduleSingle();
		demo.scheduleBoundedElastic();
		demo.scheduleParallel();
		demo.intervalSingle();
		demo.publicOnNewParallel();
		demo.subscribeOnNewParallel();

		// 让程序充分执行
		TimeUnit.SECONDS.sleep(10);
	}

	public void exeInNewThread() throws InterruptedException {
		System.out.println("> exeInNewThread:");
		// 大多数情况下，Mono和Flux在上一个操作或者顶层元运算符执行的线程中运行，比如在创建线程中执行

		// 在main线程中创建Mono
		final Mono<String> hello = Mono.just("hello");

		// 订阅在main线程中执行
		hello.map(String::toUpperCase)
				.subscribe(v -> System.out.println("原线程：" + Thread.currentThread().getName()));

		// 开启新的线程执行订阅操作，map和subscribe都在thread-0的线程中执行
		Thread thread = new Thread(() -> hello.map(String::toUpperCase)
				.subscribe(v -> System.out.println("新线程：" + Thread.currentThread().getName())));
		thread.start();
		thread.join();
	}

	public void scheduleImmediate() {
		System.out.println("> scheduleImmediate:");
		Flux<Integer> ints = Flux.just(1, 2, 3, 4, 5, 6);

		ints.flatMap(i -> Mono.just(Math.pow(i, 2)))
				.subscribeOn(Schedulers.immediate())
				// .log()
				.subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
	}

	public void scheduleSingle() {
		System.out.println("> scheduleSingle:");
		Flux<Integer> ints = Flux.just(1, 2, 3, 4, 5, 6);

		ints.flatMap(i -> Mono.just(Math.pow(i, 2)))
				.subscribeOn(Schedulers.single())
				// .log()
				.subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
	}

	public void scheduleBoundedElastic() {
		System.out.println("> scheduleBoundedElastic:");
		Flux<Integer> ints = Flux.just(1, 2, 3, 4, 5, 6);

		ints.map(i -> Math.pow(i, 2))
				.subscribeOn(Schedulers.boundedElastic())
				.subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));

		ints.flatMap(i -> Mono.just(Math.pow(i, 2)))
				.subscribeOn(Schedulers.boundedElastic())
				// .log()
				.subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
	}

	public void scheduleParallel() {
		System.out.println("> scheduleParallel:");
		Flux<Integer> ints = Flux.just(1, 2, 3, 4, 5, 6);

		Flux<Double> doubleFlux = ints.map(i -> Math.pow(i, 2))
				.subscribeOn(Schedulers.parallel())
				.log();
		// .subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
		StepVerifier.create(doubleFlux)
				.expectNext(1.0d, 4.0d, 9.0d, 16.0d, 25.0d, 36.0d)
				.verifyComplete();

		Flux<Double> doubleFlux1 = ints.flatMap(i -> Mono.just(Math.pow(i, 2)))
				.subscribeOn(Schedulers.parallel())
				.log();
		// .subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
		StepVerifier.create(doubleFlux1)
				.expectNext(1.0d, 4.0d, 9.0d, 16.0d, 25.0d, 36.0d)
				.verifyComplete();
	}

	private void intervalSingle() {
		System.out.println("> intervalSingle :");
		// 默认请的interval方法的调度策略是parallel，可以改为single
		Flux<Long> interval1 = Flux.interval(Duration.ofMillis(300)).take(5).log();
		Flux<Long> interval2 = Flux.interval(Duration.ofMillis(300), Schedulers.newSingle("test")).take(5);

		StepVerifier.create(interval1)
				.expectNext(0L, 1L, 2L, 3L, 4L)
				.verifyComplete();

		interval1.subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
		interval2.subscribe(v -> System.out.println("Thread: " + Thread.currentThread().getName() + ", v = " + v));
	}

	public void publicOnNewParallel() {
		System.out.println("> publicOnNewParallel :");

		Scheduler s = Schedulers.newParallel("public-parallel-scheduler", 4); // 创建并行Scheduler

		final Flux<String> flux = Flux
				.range(1, 2)
				.map(i -> 10 + i) // map在匿名线程中执行, 比如5
				.publishOn(s) // 切换序列在其他线程中发布，比如线程1
				.map(i -> "value " + i) // map在线程1中执行
				.log();

		new Thread(() -> flux.subscribe(System.out::println));
	}

	public void subscribeOnNewParallel() {
		System.out.println("> subscribeOnNewParallel :");

		Scheduler s = Schedulers.newParallel("subscribe-parallel-scheduler", 4);

		final Flux<String> flux = Flux
				.range(1, 2)
				.map(i -> 10 + i)
				.subscribeOn(s) // 订阅时切换到自建调度策略
				.map(i -> "value " + i)
				.log();

		new Thread(() -> flux.subscribe(System.out::println));
	}
}