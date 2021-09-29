package com.koobyte.reactor;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
	 */
	public static void main(String[] args) throws Exception {
		ScheduleDemo demo = new ScheduleDemo();
		demo.exeInNewThread();
		// demo.scheduleSingle();
		// demo.scheduleImmediate();
		// demo.scheduleBoundedElastic();
		// demo.scheduleParallel();
	}

	public void exeInNewThread() throws InterruptedException {
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
}