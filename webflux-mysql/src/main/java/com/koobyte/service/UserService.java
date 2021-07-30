package com.koobyte.service;

import com.koobyte.dao.UserDao;
import com.koobyte.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * Created by sun on 2021/7/30.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
@Service
public class UserService {
	//~ Static fields/constants/initializer


	//~ Instance fields

	@Resource
	private UserDao userDao;

	//~ Constructors


	//~ Methods

	public Mono<User> add(User user) {
		return this.userDao.save(user);
	}

	public Mono<ResponseEntity<User>> update(User user) {
		Assert.notNull(user, "User must not be null");
		Assert.notNull(user.getId(), "User id must not be null");
		return userDao.findById(user.getId())
				.flatMap(u -> this.userDao.save(u).then(Mono.just(new ResponseEntity<>(u, HttpStatus.OK))))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	public Mono<ResponseEntity<User>> delete(Long id) {
		Assert.notNull(id, "Id must not be null.");
		return userDao.findById(id)
				.flatMap(u -> userDao.deleteById(id).then(Mono.just(new ResponseEntity<>(u, HttpStatus.OK))))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	public Mono<User> findOne(Long id) {
		Assert.notNull(id, "Id must not be null.");
		return userDao.findById(id);
	}

	public Flux<User> findAll() {
		return userDao.findAll();
	}

	public Flux<User> findByName(String name) {
		Assert.hasLength(name, "Name must not be empty");
		return userDao.findByName(name);
	}
}