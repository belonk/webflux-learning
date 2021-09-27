package com.koobyte.reactor;

import reactor.core.publisher.Flux;

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

	public static void main(String[] args) {
		HandleErrorDemo demo = new HandleErrorDemo();
		demo.handleErrorAndComplete();
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

		/*
		 * 错误和完成两者不会同时执行，存在错误则执行错误回调，否则执行完成回调。执行错误或者完成流都会终止
		 */

		// 重新创建一个Flux
		ints = Flux.range(1, 4);
		// 订阅，并传递错误处理函数和执行完成函数
		ints.subscribe(System.out::println,
				error -> System.err.println("Error " + error), // 没有错误，不会执行错误函数
				() -> System.out.println("Done"));
	}
}