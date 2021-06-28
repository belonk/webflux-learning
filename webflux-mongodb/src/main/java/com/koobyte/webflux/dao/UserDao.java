package com.koobyte.webflux.dao;

import com.koobyte.webflux.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Created by sun on 2021/6/28.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
public interface UserDao extends ReactiveMongoRepository<User, Long> {
	//~ Constants/Initializer


	//~ Interfaces


}
