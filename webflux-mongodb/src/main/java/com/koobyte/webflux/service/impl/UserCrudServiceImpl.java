package com.koobyte.webflux.service.impl;

import com.koobyte.webflux.dao.UserDao;
import com.koobyte.webflux.domain.User;
import com.koobyte.webflux.service.UserCrudService;
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
	public Mono<User> update(User user) {
		assert user != null && user.getId() != null;
		return userDao.save(user);
	}

	@Override
	public Mono<Void> delete(Long id) {
		return userDao.deleteById(id);
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