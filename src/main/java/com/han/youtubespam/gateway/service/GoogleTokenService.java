package com.han.youtubespam.gateway.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.han.youtubespam.gateway.consts.RedisConstant;
import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.type.GoogleTokenResponse;
import com.han.youtubespam.gateway.utils.AesCodec;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleTokenService {

	@Qualifier("googleClient")
	private final WebClient googleClient;

	private final RedisService redisService;
	private final AesCodec aesCodec;
	private final ClientRegistrationRepository clientRegistrationRepository;

	public String renewGoogleAccessToken(MemberEntity member) {
		ClientRegistration google = clientRegistrationRepository.findByRegistrationId("google");

		String refreshToken = aesCodec.decrypt(member.getGoogleRt());

		GoogleTokenResponse tokenResponse = googleClient.post()
			.uri("/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters
				.fromFormData("client_id", google.getClientId())
				.with("client_secret", google.getClientSecret())
				.with("refresh_token", refreshToken)
				.with("grant_type", "refresh_token")
			).retrieve()
			.bodyToMono(GoogleTokenResponse.class)
			.block();

		this.setAccessToken(member.getUuid(), tokenResponse.accessToken());
		return tokenResponse.accessToken();

	}

	public void revokeGoogleToken(String refreshToken) {
		googleClient.post()
			.uri("/revoke")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("token", refreshToken))
			.retrieve()
			.toBodilessEntity()
			.block();
	}

	public String getAccessToken(UUID uuid) {
		return redisService.get(RedisConstant.REDIS_GOOGLE_ACCESS_KEY + uuid).orElse("");
	}

	public String setAccessToken(UUID uuid, String accessToken) {
		redisService.set(
			RedisConstant.REDIS_GOOGLE_ACCESS_KEY + uuid,
			accessToken,
			Duration.ofMinutes(59)
		);
		return accessToken;
	}
}
