package com.han.youtubespam.gateway.resolver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.han.youtubespam.gateway.consts.JwtConstant;
import com.han.youtubespam.gateway.provider.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
	private final OAuth2AuthorizationRequestResolver delegate;
	private final JwtProvider jwtProvider;

	public JwtAuthorizationRequestResolver(
		ClientRegistrationRepository repo,
		JwtProvider jwtProvider
	) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
			repo, "/oauth2/authorization"
		);
		this.jwtProvider = jwtProvider;
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		OAuth2AuthorizationRequest base = delegate.resolve(request);
		return customize(request, base);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(
		HttpServletRequest request,
		String registrationId
	) {
		OAuth2AuthorizationRequest base =
			delegate.resolve(request, registrationId);
		return customize(request, base);
	}

	private OAuth2AuthorizationRequest customize(
		HttpServletRequest request,
		OAuth2AuthorizationRequest base
	) {
		if (base == null)
			return null;

		Map<String, Object> params = new HashMap<>(base.getAdditionalParameters());
		params.put("access_type", "offline");

		String auth = request.getHeader("Authorization");
		if (auth != null && auth.startsWith("Bearer ")) {
			String accessToken = auth.substring(7);
			if (!jwtProvider.validate(accessToken, JwtConstant.JWT_TYPE_ACCESS)
				|| jwtProvider.getUserId(accessToken) == null)
				params.put("prompt", "consent");
		} else {
			params.put("prompt", "consent");
		}

		return OAuth2AuthorizationRequest.from(base)
			.additionalParameters(params)
			.build();
	}

}
