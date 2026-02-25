package com.han.youtubespam.gateway.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
	private final OAuth2AuthorizationRequestResolver delegate;

	public JwtAuthorizationRequestResolver(
		ClientRegistrationRepository repo
	) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
			repo, "/oauth2/authorization"
		);
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

		String registrationId = request.getServletPath()
			.replace("/oauth2/authorization/", "");

		return switch (registrationId) {
			case "google" -> _google(request, base);
			default -> _base(base);
		};
	}

	private OAuth2AuthorizationRequest _base(
		OAuth2AuthorizationRequest base
	) {
		return OAuth2AuthorizationRequest.from(base).build();
	}

	private OAuth2AuthorizationRequest _google(
		HttpServletRequest request,
		OAuth2AuthorizationRequest base
	) {
		Map<String, Object> params = new HashMap<>(base.getAdditionalParameters());
		params.put("access_type", "offline");

		Set<String> scopes = new HashSet<>(base.getScopes());
		String mode = request.getParameter("mode");
		if ("connect".equals(mode))
			scopes.add("https://www.googleapis.com/auth/youtube.force-ssl");

		return OAuth2AuthorizationRequest.from(base)
			.additionalParameters(params)
			.scopes(scopes)
			.build();
	}

}
