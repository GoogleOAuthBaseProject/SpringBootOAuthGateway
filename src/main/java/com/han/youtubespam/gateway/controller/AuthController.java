package com.han.youtubespam.gateway.controller;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.han.youtubespam.gateway.consts.CookieConstant;
import com.han.youtubespam.gateway.consts.HeaderConstant;
import com.han.youtubespam.gateway.consts.JwtConstant;
import com.han.youtubespam.gateway.exception.InvalidTokenException;
import com.han.youtubespam.gateway.provider.JwtProvider;
import com.han.youtubespam.gateway.type.TokenPair;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final JwtProvider jwtProvider;

	@PostMapping("/complete")
	public ResponseEntity<?> oAuthCompleteAndGenToken(
		@CookieValue(value = CookieConstant.OAUTH_NONCE_TOKEN) String tt
	) {
		if (tt == null || !jwtProvider.validate(tt, JwtConstant.JWT_TYPE_TEMP)) {
			return ResponseEntity
				.status(401)
				.header("X-Auth-Error", "REQUIRE_VALID_TEMP_TOKEN")
				.build();
		}

		UUID memberId = jwtProvider.getUserId(tt);
		TokenPair tokenPair = jwtProvider.reissue(memberId);

		ResponseCookie refreshTokenCookie = ResponseCookie
			.from(CookieConstant.REFRESH_TOKEN, tokenPair.refreshToken())
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.path("/")
			.maxAge(Duration.ofDays(14))
			.build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
			.body(Map.of(
				"accessToken", tokenPair.accessToken()
			));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(
		@CookieValue(value = CookieConstant.REFRESH_TOKEN, required = false) String rt
	) {
		if (rt == null || !jwtProvider.validate(rt, JwtConstant.JWT_TYPE_REFRESH)) {
			return ResponseEntity
				.status(401)
				.header("X-Auth-Error", "REFRESH_TOKEN_EXPIRED")
				.build();
		}
		Claims claims = jwtProvider.getClaims(rt);
		if ("at".equals(claims.get(JwtConstant.JWT_TYPE, String.class))) {
			throw new InvalidTokenException("Not Refresh Token");
		}
		UUID userId = UUID.fromString(claims.getSubject());
		TokenPair tokenPair = jwtProvider.reissue(userId);

		ResponseCookie refreshTokenCookie = ResponseCookie.from(
				CookieConstant.REFRESH_TOKEN, tokenPair.refreshToken()
			)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path("/auth/refresh")
			.maxAge(Duration.ofDays(14))
			.build();

		return ResponseEntity.ok()
			.header(HeaderConstant.SET_COOKIE, refreshTokenCookie.toString())
			.body(Map.of("accessToken", tokenPair.accessToken()));
	}
}
