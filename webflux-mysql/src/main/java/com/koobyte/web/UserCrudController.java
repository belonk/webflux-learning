package com.koobyte.web;

import com.koobyte.entity.User;
import com.koobyte.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * Created by sun on 2021/6/27.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
@RestController
@RequestMapping("/user")
public class UserCrudController {
	//~ Static fields/constants/initializer


	//~ Instance fields

	@Resource
	private UserService userService;

	//~ Constructors


	//~ Methods

	@PostMapping
	public Mono<User> add(@RequestBody User user) {
		return userService.add(user);
	}

	@PutMapping
	public Mono<ResponseEntity<User>> update(@RequestBody User user) {
		return userService.update(user);
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<User>> delete(@PathVariable Long id) {
		return userService.delete(id);
	}

	@GetMapping("/{id}")
	public Mono<User> findOne(@PathVariable Long id) {
		return userService.findOne(id);
	}

	@GetMapping("/all")
	public Flux<User> findAll() {
		return userService.findAll();
	}

	@GetMapping(value = "/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<User> findStreamAll() {
		return userService.findAll();
	}

	@GetMapping("/name")
	public Flux<User> findByName(String name) {
		return userService.findByName(name);
	}
}