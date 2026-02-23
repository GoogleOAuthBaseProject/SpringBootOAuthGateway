package com.han.youtubespam.gateway.type;

public record JWTTokenPair(
	String accessToken,
	String refreshToken
) {
}
