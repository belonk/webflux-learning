package com.koobyte.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koobyte.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Commit;
import org.springframework.web.client.RestTemplate;

/**
 * Created by sun on 2021/6/28.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
public class UserControllerTest {
	//~ Static fields/constants/initializer


	//~ Instance fields

	RestTemplate restTemplate = new RestTemplate();
	static final String URL = "http://localhost:8080/user";
	ObjectMapper mapper = new ObjectMapper();

	//~ Constructors


	//~ Methods

	@Test
	public void testAdd() {
		User user = new User();
		user.setName("张三");
		user.setAge(20);
		String body = restTemplate.postForEntity(URL, user, String.class).getBody();
		System.out.println(body);
	}

	@Test
	public void testAdd2() {
		User user = new User();
		user.setName("李四");
		user.setAge(30);
		String body = restTemplate.postForEntity(URL, user, String.class).getBody();
		System.out.println(body);
	}

	@Test
	public void testUpdate() {
		User user = new User();
		user.setId(7L);
		user.setName("李四");
		user.setAge(33); // 修改年龄
		restTemplate.put(URL, user, String.class);
	}

	@Test
	public void testUpdateNotFound() {
		User user = new User();
		user.setId(100L);
		user.setName("王一百");
		user.setAge(100);
		restTemplate.put(URL, user, String.class);
	}

	@Test
	public void testGet() {
		User user = restTemplate.getForObject(URL + "/" + 6, User.class);
		System.out.println(user);
		User user1 = restTemplate.getForObject(URL + "/" + 7, User.class);
		System.out.println(user1);
	}

	@Test
	public void testDeleteSuccessfully() {
		// 删除成功，返回200
		restTemplate.delete(URL + "/" + 7L);
	}

	@Test
	public void testDeleteFailed() {
		// 删除失败，返回404 no body
		restTemplate.delete(URL + "/" + 100L);
	}

	@Test
	public void testGetAll() throws JsonProcessingException {
		String body = restTemplate.getForEntity(URL + "/all", String.class).getBody();
		System.out.println(body);
	}

	@Test
	public void testGetStreamAll() throws JsonProcessingException {
		String body = restTemplate.getForEntity(URL + "/stream/all", String.class).getBody();
		System.out.println(body);
	}
}