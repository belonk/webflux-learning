package com.koobyte;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 2021/6/27.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
@WebServlet(urlPatterns = "/sync")
public class SyncServlet extends HttpServlet {
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
		// 模拟耗时操作
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("执行线程：" + Thread.currentThread().getName());
		// 3秒后再页面输出
		PrintWriter writer = resp.getWriter();
		writer.write("Hello Servlet!\n");
		writer.flush();
		System.out.println(Thread.currentThread().getName() + ", 耗时：" + (System.currentTimeMillis() - start) + " ms");
		// 页面等待3秒后再输出，耗时时间3秒多
		// 耗时：3004 ms
	}
}