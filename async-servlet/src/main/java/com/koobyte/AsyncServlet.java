package com.koobyte;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2021/6/27.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
// asyncSupported: 开启异步支持
@WebServlet(urlPatterns = "/async", asyncSupported = true)
public class AsyncServlet extends HttpServlet {
	//~ Static fields/constants/initializer


	//~ Instance fields


	//~ Constructors


	//~ Methods


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		long start = System.currentTimeMillis();
		// 开启异步
		AsyncContext asyncContext = req.startAsync();
		CompletableFuture.runAsync(() -> {
			// 模拟耗时操作
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 3秒后再输出
			PrintWriter writer = null;
			try {
				// 执行线程：ForkJoinPool.commonPool-worker-1
				System.out.println("执行线程：" + Thread.currentThread().getName());
				writer = asyncContext.getResponse().getWriter();
				writer.write("Hello Async Servlet!\n");
				writer.flush();
				// 异步执行完成
				asyncContext.complete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		System.out.println(Thread.currentThread().getName() + ", 耗时：" + (System.currentTimeMillis() - start) + " ms");
		// 后台记录耗时时间
		// 页面仍然是等待3秒后再输出，但是耗时时间很短
		// 耗时：11 ms
	}
}