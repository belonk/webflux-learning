package com.koobyte.reactor;

import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by sun on 2021/9/27.
 *
 * @author sunfuchang03@126.com
 * @since 3.0
 */
public class HandleErrorDemo {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods

	public static void main(String[] args) throws InterruptedException {
		/*
		 * 错误和完成两者不会同时执行，存在错误则执行错误回调，否则执行完成回调。执行错误或者完成流都会终止
		 */
		HandleErrorDemo demo = new HandleErrorDemo();
		demo.onErrorReturn();
		demo.onErrorResume();
		demo.onErrorMap();
		demo.doOnError();
		demo.handleErrorAndComplete();
		demo.doFinally();
		demo.using();
		demo.retry();
		demo.retryWhen();
		demo.retrySignal();
		demo.transientError();
	}

	public void onErrorReturn() {
		System.out.println("> onErrorReturn :");

		// 静态回退值，通过onErrorReturn在出错时重新返回一个新值

		Flux<String> stringFlux = Flux.just(1, 2, 0, 3)
				.map(i -> "100 / " + i + " = " + (100 / i)) // 触发错误
				.onErrorReturn("Divided by zero :(");// 抛出异常时返回一个新值，然后Flux序列终止

		stringFlux.subscribe(System.out::println);
		// 100 / 1 = 100
		// 100 / 2 = 50
		// Divided by zero :(
	}

	public void onErrorResume() {
		System.out.println("> onErrorResume :");

		// 回退方法，通过onErrorResume在出错时可以通过Function参数重新处理回退逻辑

		Flux<Integer> integerFlux = Flux.just(1, 2, 0, 3)
				.map(i -> 100 / i)
				.onErrorResume(new Function<Throwable, Publisher<? extends Integer>>() {
					@Override
					public Publisher<Integer> apply(Throwable throwable) {
						// 重新处理逻辑，这里为了简便直接返回
						return Flux.just(-1);
					}
				});
		// 100 50 -1
		integerFlux.subscribe(System.out::println);

		// 通过onErrorResume切换不同的回退序列
		integerFlux = Flux.just(1, 2, 0, 3)
				.map(i -> 100 / i)
				.onErrorResume(new Function<Throwable, Publisher<? extends Integer>>() {
					@Override
					public Publisher<Integer> apply(Throwable throwable) {
						if (throwable instanceof ArithmeticException) {
							return Flux.just(-1); // 返回-1
						} else if (throwable instanceof RuntimeException) {
							return Flux.just(0); // 返回0
						} else {
							return Flux.error(throwable); // 重新抛出异常
						}
					}
				});
		// 100 50 -1
		integerFlux.subscribe(System.out::println);
	}

	public void onErrorMap() {
		System.out.println("> onErrorMap :");

		Flux<Integer> integerFlux = Flux.just(1, 2, 0, 3)
				.map(i -> 100 / i)
				// 重新包装异常并抛出
				.onErrorMap(throwable -> new RuntimeException("divided by zero", throwable));
		integerFlux.subscribe(System.out::println);
	}

	public void doOnError() {
		System.out.println("> doOnError :");

		Flux<Integer> integerFlux = Flux.just(1, 2, 0, 3)
				.map(i -> 100 / i)
				// 记录错误信息
				.doOnError(new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) {
						// 简单打印错误信息，类似log.error(xxx)
						System.out.println(throwable);
					}
				});
		integerFlux.subscribe(System.out::println);
	}

	public void handleErrorAndComplete() {
		System.out.println("> handleErrorAndComplete :");
		// 创建整数序列的Flux，从1开始，共4个元素
		Flux<Integer> ints = Flux.range(1, 4)
				// 处理每个元素，如果i<=3正常返回，否则抛出异常
				.map(i -> {
					if (i <= 3) return i;
					throw new RuntimeException("Got to 4");
				});
		// 订阅Flux，并且传递错误处理回调函数，打印错误信息
		ints.subscribe(System.out::println, error -> System.err.println("Error: " + error));

		// 重新创建一个Flux
		ints = Flux.range(1, 4);
		// 订阅，并传递错误处理函数和执行完成函数
		ints.subscribe(System.out::println,
				error -> System.err.println("Error " + error), // 没有错误，不会执行错误函数
				() -> System.out.println("Done"));
	}

	public void doFinally() {
		System.out.println("> doFinally :");

		// doFinally是:在序列终止（使用onComplete或onError）或被取消时执行

		Flux<Integer> integerFlux = Flux.just(1, 2, 0, 3)
				.map(i -> 100 / i)
				.onErrorResume(throwable -> Flux.just(-1))
				// 处理完成逻辑
				.doFinally(new Consumer<SignalType>() {
					@Override
					public void accept(SignalType signalType) {
						// 依据序列的处理类型来打印不同的信息
						if (signalType == SignalType.ON_COMPLETE) {
							// 完成时
							System.out.println("complete");
						} else if (signalType == SignalType.ON_ERROR) {
							// 出错且没有错误处理时
							System.out.println("error");
						} else if (signalType == SignalType.CANCEL) {
							// 序列取消时
							System.out.println("cancel");
						}
					}
				});
		// 100 50 -1 complete
		integerFlux.subscribe(System.out::println);
		// 如果注释掉onErrorResume，则输出：100 50 错误信息 error
	}

	public void using() {
		System.out.println("> using :");

		// using可以处理类似命令式中的try with resource语句

		AtomicBoolean isDisposed = new AtomicBoolean();
		// 创建Disposable
		Disposable disposableInstance = new Disposable() {
			@Override
			public void dispose() {
				// 清理资源并设置为true
				isDisposed.set(true);
			}

			@Override
			public String toString() {
				return "DISPOSABLE";
			}
		};

		// 反应式中用using方法尝试使用资源
		Flux<String> flux = Flux.using(
				() -> disposableInstance, // 生成资源
				disposable -> Flux.just(disposable.toString()), // 处理资源
				Disposable::dispose // 清理资源
		);

		// DISPOSABLE
		flux.subscribe(System.out::println, throwable -> {
		}, () -> System.out.println(isDisposed.get())); // 输出：true
	}

	public void retry() throws InterruptedException {
		System.out.println("> retry :");

		// 没250ms发送一个数据
		Flux.interval(Duration.ofMillis(250))
				.map(input -> {
					if (input < 3)
						return "tick " + input;
					throw new RuntimeException("boom");
				})
				.retry(1) // 如果出错，则重试一次，重试其实就是重新订阅一次，从0开始计时
				.elapsed() // 将每个值与自上一个值发出以来的持续时间相关联
				.subscribe(System.out::println, System.err::println);

		// 保证interval后不会立即退出
		Thread.sleep(2100);

		/*~:
		[257,tick 0]
		[246,tick 1]
		[253,tick 2]
		[504,tick 0]
		[250,tick 1]
		[250,tick 2]
		java.lang.RuntimeException: boom
		 */
	}

	public void retryWhen() {
		System.out.println("> retryWhen :");

		Flux.<String>error(new IllegalArgumentException()) // 不断产生错误
				.doOnError(System.out::println) // 打印异常
				// 定义重试策略：前三个错误重试，然后放弃
				.retryWhen(new Retry() { // 效果同：Retry.from(companion -> companion.take(3))
					@Override
					public Publisher<?> generateCompanion(Flux<RetrySignal> retrySignals) {
						// 获取前三个重试信号并重试，第4个错误丢弃，序列终止
						return retrySignals.take(3);
					}
				})
				.elapsed()
				.subscribe(System.out::println); // 订阅并打印元素
		/*~:
		一共输出4个错误信息：
		java.lang.IllegalArgumentException
		java.lang.IllegalArgumentException
		java.lang.IllegalArgumentException
		java.lang.IllegalArgumentException
		 */
	}

	public void retrySignal() {
		System.out.println("> retrySignal :");

		AtomicInteger errorCount = new AtomicInteger();
		Flux.<String>error(new IllegalArgumentException()) // 创建一个不断产生错误的Flux序列
				.doOnError(new Consumer<Throwable>() { // 错误处理逻辑
					@Override
					public void accept(Throwable throwable) {
						errorCount.getAndIncrement();
					}
				})
				// 自定义重新策略
				.retryWhen(Retry.from(new Function<Flux<Retry.RetrySignal>, Flux<Long>>() {
					@Override
					public Flux<Long> apply(Flux<Retry.RetrySignal> retrySignalFlux) {
						// 使用map函数处理flux，获取到RetrySignal对象
						return retrySignalFlux.map(new Function<Retry.RetrySignal, Long>() {
							@Override
							public Long apply(Retry.RetrySignal retrySignal) {

								// 根据错误重试次数决定是否抛出原始异常，并终止序列

								System.out.println("序列值：" + retrySignal.totalRetries());
								// 重试次数小于3，则返回一个值，这里简单的反悔了重试的次数
								if (retrySignal.totalRetries() < 3) {
									return retrySignal.totalRetries();
								} else {
									// 打印重试时返回的值
									System.out.println("错误次数：" + errorCount.get());
									// 重试次数大于等于3，则抛出原始异常，序列终止
									throw Exceptions.propagate(retrySignal.failure());
								}
							}
						});
					}
				})).subscribe();

		/*~
		序列值：0
		序列值：1
		序列值：2
		序列值：3
		错误次数：4
		[ERROR] (main) Operator called default onErrorDropped - reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalArgumentException
		reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalArgumentException
		Caused by: java.lang.IllegalArgumentException
		Caused by: java.lang.IllegalArgumentException

			at com.koobyte.reactor.HandleErrorDemo.retrySignal(HandleErrorDemo.java:258)
			at com.koobyte.reactor.HandleErrorDemo.main(HandleErrorDemo.java:48)

		 */
	}

	public void transientError() {
		System.out.println("> transientError :");

		AtomicInteger errorCount = new AtomicInteger();
		AtomicInteger transientHelper = new AtomicInteger();
		// 不断生成序列
		Flux<Integer> transientFlux = Flux.<Integer>generate(sink -> {
			int i = transientHelper.getAndIncrement();
			// i增加到10就完成序列
			if (i == 10) {
				sink.next(i);
				sink.complete();
			} else if (i % 3 == 0) {
				// i是3的倍数，直接发送下一个序列
				sink.next(i);
			} else {
				// 否则，抛出异常，抛出异常的值：1,2,4,5,7,8
				sink.error(new IllegalStateException("Transient error at " + i));
			}
		}).doOnError(e -> errorCount.incrementAndGet()); // 出错，errorCount加1

		// 设置瞬态错误模式，指示正在构建的策略应使用Retry.RetrySignal.totalRetriesInARow()而不是Retry.RetrySignal.totalRetries() 。
		// 瞬态错误是可能发生在突发中的错误，但在另一个错误发生之前通过重试（使用一个或多个 onNext 信号）将其恢复。
		transientFlux.retryWhen(Retry.max(2)
				.transientErrors(true) // 设置瞬态错误模式
		).blockLast(); // blockLast：订阅此Flux并无限期阻止，直到上游发出其最后一个值或完成为止
		System.out.println(errorCount);
		/*~:
		输出：6
		如果注释掉transientErrors(true)，那么错误将重试2次，达到重试次数然后抛出异常：IllegalStateException : Transient error at 4
		 */
	}
}