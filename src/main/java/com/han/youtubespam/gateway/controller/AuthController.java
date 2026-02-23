package com.han.youtubespam.gateway.controller;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.han.youtubespam.gateway.consts.CookieConstant;
import com.han.youtubespam.gateway.consts.HeaderConstant;
import com.han.youtubespam.gateway.consts.JwtConstant;
import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.exception.InvalidTokenException;
import com.han.youtubespam.gateway.provider.JwtProvider;
import com.han.youtubespam.gateway.service.GoogleTokenService;
import com.han.youtubespam.gateway.service.MemberService;
import com.han.youtubespam.gateway.type.ChannelDataPair;
import com.han.youtubespam.gateway.type.JWTTokenPair;
import com.han.youtubespam.gateway.type.youtube_data.YoutubeChannelResponseDto;
import com.han.youtubespam.gateway.utils.TokenUtil;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final JwtProvider jwtProvider;

	@Qualifier("youtubeClient")
	private final WebClient youtubeClient;

	@Qualifier("googleClient")
	private final WebClient googleClient;

	private final MemberService memberService;
	private final GoogleTokenService googleTokenService;

	@Transactional
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

		UUID memberId = jwtProvider.getMemberId(tt);
		MemberEntity member = memberService.getMember(memberId);
		JWTTokenPair JWTTokenPair = jwtProvider.reissue(member);

		if (member.isHasYoutubeAccess()) {
			YoutubeChannelResponseDto channelDto = youtubeClient.get()
				.uri("/channels/mine")
				.header("Authorization", "Bearer " + googleTokenService.getAccessToken(memberId))
				.retrieve()
				.bodyToMono(YoutubeChannelResponseDto.class)
				.block();
			assert channelDto != null;
			member.setChannelData(new ChannelDataPair(channelDto.data().get(0)));
		}

		ResponseCookie tempTokenCookieClear = TokenUtil.revokeResponseCookie(CookieConstant.OAUTH_NONCE_TOKEN);
		ResponseCookie refreshTokenCookie = TokenUtil.genResponseCookie(CookieConstant.REFRESH_TOKEN,
			JWTTokenPair.refreshToken(), Duration.ofDays(14));

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
			.header(HttpHeaders.SET_COOKIE, tempTokenCookieClear.toString())
			.body(Map.of(
				"accessToken", JWTTokenPair.accessToken()
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
		UUID memberId = UUID.fromString(claims.getSubject());
		MemberEntity memberEntity = memberService.getMember(memberId);
		JWTTokenPair JWTTokenPair = jwtProvider.reissue(memberEntity);

		ResponseCookie refreshTokenCookie = TokenUtil.genResponseCookie(CookieConstant.REFRESH_TOKEN,
			JWTTokenPair.refreshToken(), Duration.ofDays(14));

		return ResponseEntity.ok()
			.header(HeaderConstant.SET_COOKIE, refreshTokenCookie.toString())
			.body(Map.of("accessToken", JWTTokenPair.accessToken()));
	}
}
