package com.koobyte.webflux.service;

import com.koobyte.webflux.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by sun on 2021/6/28.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
public interface UserCrudService {
	//~ Constants/Initializer


	//~ Interfaces

	Mono<User> add(User user);

	Flux<User> add(List<User> users);

	Mono<User> update(User user);

	Mono<Void> delete(Long id);

	Mono<User> findOne(Long id);

	Flux<User> findAll();
}
