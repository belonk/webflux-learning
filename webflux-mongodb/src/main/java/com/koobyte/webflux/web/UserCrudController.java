package com.koobyte.webflux.web;

import com.koobyte.webflux.domain.User;
import com.koobyte.webflux.service.UserCrudService;
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
	private UserCrudService userCrudService;

	//~ Constructors


	//~ Methods

	@PostMapping
	public Mono<User> add(@RequestBody User user) {
		return userCrudService.add(user);
	}

	@PutMapping
	public Mono<ResponseEntity<User>> update(@RequestBody User user) {
		return userCrudService.update(user);
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
		return userCrudService.delete(id);
	}

	@GetMapping("/{id}")
	public Mono<User> findOne(@PathVariable Long id) {
		return userCrudService.findOne(id);
	}

	@GetMapping("/all")
	public Flux<User> findAll() {
		return userCrudService.findAll();
	}

	@GetMapping(value = "/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<User> findStreamAll() {
		return userCrudService.findAll();
	}
}