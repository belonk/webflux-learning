package com.koobyte.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Created by sun on 2021/7/30.
 *
 * @author sunfuchang03@126.com
 * @since 1.0
 */
@Table("user")
public class User {
	//~ Static fields/constants/initializer


	//~ Instance fields

	@Id
	private Long id;
	private String name;
	private Integer age;
	private String email;

	//~ Constructors


	//~ Methods

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", age=" + age +
				", email='" + email + '\'' +
				'}';
	}
}