package com.han.youtubespam.gateway.type;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

public record GoogleTokenPair(
	OAuth2AccessToken accessToken,
	OAuth2RefreshToken refreshToken
) {
}
