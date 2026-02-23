package com.han.youtubespam.gateway.utils;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

public class TokenUtil {
	public static ResponseCookie genResponseCookie(String name, String value, Duration maxAge) {
		return ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.path("/")
			.maxAge(maxAge)
			.build();
	}

	public static ResponseCookie revokeResponseCookie(String name) {
		return genResponseCookie(name, "", Duration.ofSeconds(0));
	}
}
