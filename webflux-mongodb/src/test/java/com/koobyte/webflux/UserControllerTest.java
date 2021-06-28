package com.koobyte.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koobyte.webflux.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
		user.setId(1L);
		user.setName("张三");
		user.setAge(20);
		String body = restTemplate.postForEntity(URL, user, String.class).getBody();
		System.out.println(body);
	}

	@Test
	public void testAdd2() {
		User user = new User();
		user.setId(2L);
		user.setName("李四");
		user.setAge(30);
		String body = restTemplate.postForEntity(URL, user, String.class).getBody();
		System.out.println(body);
	}

	@Test
	public void testUpdate() {
		User user = new User();
		user.setId(2L);
		user.setName("李四");
		user.setAge(33);
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
		User user = restTemplate.getForObject(URL + "/" + 1, User.class);
		System.out.println(user);
		User user1 = restTemplate.getForObject(URL + "/" + 2, User.class);
		System.out.println(user1);
	}

	@Test
	public void testDeleteSuccessfully() {
		// 删除成功，返回200
		restTemplate.delete(URL + "/" + 2L);
	}

	@Test
	public void testDeleteFailed() {
		// 删除失败，返回404 no body
		restTemplate.delete(URL + "/" + 100L);
	}

	@Test
	public void testGetAll() throws JsonProcessingException {
		String body = restTemplate.getForEntity(URL + "/all", String.class).getBody();
		System.out.println(mapper.readValue(body, new TypeReference<List<User>>() {
		}));
	}
}