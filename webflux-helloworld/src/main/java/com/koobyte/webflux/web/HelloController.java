package com.koobyte.webflux.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2021/6/27.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
@RestController
@RequestMapping("/hello")
public class HelloController {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods

	// 原始同步方法
	@GetMapping
	public String sayHello() {
		long start = System.currentTimeMillis();
		String s = print();
		// 传统方式耗时：2003 ms
		System.out.println("传统方式耗时：" + (System.currentTimeMillis() - start) + " ms");
		return "No " + s;

	}

	// 返回mono，表示0个或1个数据
	@GetMapping("/mono")
	public Mono<String> monoHello() {
		long start = System.currentTimeMillis();
		String s = "Mono " + print();
		Mono<String> mono = Mono.just(s);
		// Mono耗时：2001 ms
		System.out.println("Mono耗时：" + (System.currentTimeMillis() - start) + " ms");
		return mono;
	}

	private String print() {
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "webflux";
	}

	// Flux表示一个或多个
	// 响应Content-Type设置为：text/event-stream，表示按流返回数据，现象是一条条逐步返回数据
	// 不设置，则是一次性返回所有元素
	@GetMapping(value = "/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> fluxHello() {
		long start = System.currentTimeMillis();
		// 定义一个数组
		String[] ss = {"a", "b", "c", "d"};
		// 从数组构建Flux对象，每一个元素返回前睡眠2秒
		Flux<String> flux = Flux.fromArray(ss).map(s -> {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return "数据元素：" + s;
		});
		// Flux耗时：2 ms
		System.out.println("Flux耗时：" + (System.currentTimeMillis() - start) + " ms");
		return flux;
		/*:
		每隔两秒输出一行：
		data:数据元素：a
		data:数据元素：b
		data:数据元素：c
		data:数据元素：d
		 */
	}
}