package com.han.youtubespam.gateway.provider;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.han.youtubespam.gateway.consts.JwtConstant;
import com.han.youtubespam.gateway.consts.TimeConstant;
import com.han.youtubespam.gateway.type.TokenPair;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {
	private static final long EXP_ACCESS_TOKEN = TimeConstant.EXP_MILLIS_HOUR * 6;
	private static final long EXP_REFRESH_TOKEN = TimeConstant.EXP_MILLIS_DAY * 14;
	private final Key key;

	public JwtProvider(
		@Value("${app.security.secret.jwt}") String secret
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String issue(UUID userId, String type) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + EXP_ACCESS_TOKEN);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(JwtConstant.JWT_TYPE, type)
			.issuedAt(now)
			.expiration(exp)
			.signWith(key)
			.compact();
	}

	public String issue(UUID userId, String type, Date now, Long expMillis) {
		Date exp = new Date(now.getTime() + expMillis);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(JwtConstant.JWT_TYPE, type)
			.issuedAt(now)
			.expiration(exp)
			.signWith(key)
			.compact();
	}

	public TokenPair reissue(UUID userId) {
		Date now = new Date();
		String accessToken = issue(userId, "at", now, EXP_ACCESS_TOKEN);
		String refreshToken = issue(userId, "rt", now, EXP_REFRESH_TOKEN);

		return new TokenPair(accessToken, refreshToken);
	}

	public Claims getClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith((SecretKey)key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean validate(String token, String type) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith((SecretKey)key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
			return type.equals(claims.get(JwtConstant.JWT_TYPE));
		} catch (Exception e) {
			return false;
		}
	}

	public UUID getUserId(String token) {
		try {
			Claims claims = getClaims(token);
			return UUID.fromString(claims.getSubject());
		} catch (Exception e) {
			return null;
		}
	}
}
