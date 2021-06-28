package com.koobyte.webflux.service.impl;

import com.koobyte.webflux.dao.UserDao;
import com.koobyte.webflux.domain.User;
import com.koobyte.webflux.service.UserCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by sun on 2021/6/28.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
@Service
public class UserCrudServiceImpl implements UserCrudService {
	//~ Static fields/constants/initializer


	//~ Instance fields

	@Resource
	private UserDao userDao;

	//~ Constructors


	//~ Methods

	@Override
	public Mono<User> add(User user) {
		return userDao.insert(user);
	}

	@Override
	public Flux<User> add(List<User> users) {
		return userDao.insert(users);
	}

	@Override
	public Mono<ResponseEntity<User>> update(User user) {
		assert user != null && user.getId() != null;
		return userDao.findById(user.getId())
				// 找到，直接修改
				.flatMap(u -> userDao.save(u).then(Mono.just(new ResponseEntity<>(u, HttpStatus.OK))))
				// 未找到，返回404
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@Override
	public Mono<ResponseEntity<Void>> delete(Long id) {
		return userDao.findById(id) // 查询出数据
				// 查询到了则删除，返回200
				.flatMap(user -> userDao.deleteById(user.getId()).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
				// 没有找到返回404
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@Override
	public Mono<User> findOne(Long id) {
		return userDao.findById(id);
	}

	@Override
	public Flux<User> findAll() {
		return userDao.findAll();
	}
}