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
import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.entity.MemberRole;
import com.han.youtubespam.gateway.type.JWTTokenPair;

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

	public String issueAt(UUID userId, MemberRole role, String type, Date now, Long expMillis) {
		Date exp = new Date(now.getTime() + expMillis);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(JwtConstant.JWT_TYPE, type)
			.claim(JwtConstant.JWT_TYPE_ROLE, role.name())
			.issuedAt(now)
			.expiration(exp)
			.signWith(key)
			.compact();
	}

	public String issueRtTt(UUID userId, String type, Date now, Long expMillis) {
		Date exp = new Date(now.getTime() + expMillis);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(JwtConstant.JWT_TYPE, type)
			.issuedAt(now)
			.expiration(exp)
			.signWith(key)
			.compact();
	}

	public JWTTokenPair reissue(MemberEntity memberEntity) {
		Date now = new Date();
		String accessToken = issueAt(memberEntity.getUuid(), memberEntity.getRole(), "at", now, EXP_ACCESS_TOKEN);
		String refreshToken = issueRtTt(memberEntity.getUuid(), "rt", now, EXP_REFRESH_TOKEN);

		return new JWTTokenPair(accessToken, refreshToken);
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

	public UUID getMemberId(String token) {
		try {
			Claims claims = getClaims(token);
			return UUID.fromString(claims.getSubject());
		} catch (Exception e) {
			return null;
		}
	}

	public MemberRole getMemberRole(String token) {
		try {
			Claims claims = getClaims(token);
			String roleName = claims.get(JwtConstant.JWT_TYPE_ROLE, String.class);
			return MemberRole.valueOf(roleName);
		} catch (Exception e) {
			return null;
		}
	}
}
