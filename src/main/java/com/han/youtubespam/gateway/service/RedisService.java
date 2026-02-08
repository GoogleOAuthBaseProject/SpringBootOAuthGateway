package com.han.youtubespam.gateway.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.lang.Nullable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, String> redisTemplate;

	public void set(
		String key,
		String data,
		@Nullable Duration ttl
	) {
		if (ttl == null)
			redisTemplate.opsForValue().set(key, data);
		else
			redisTemplate.opsForValue().set(key, data, ttl);
	}

	public Optional<String> get(String key) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(key));
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public void persist(String key) {
		redisTemplate.persist(key);
	}
}
