package com.han.youtubespam.gateway.type;

public record TokenPair(
	String accessToken,
	String refreshToken
) {
}
