package com.han.youtubespam.gateway.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.han.youtubespam.gateway.consts.CookieConstant;
import com.han.youtubespam.gateway.consts.FilterConstant;
import com.han.youtubespam.gateway.consts.GoogleConstant;
import com.han.youtubespam.gateway.consts.JwtConstant;
import com.han.youtubespam.gateway.consts.TimeConstant;
import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.property.ServerIPProperties;
import com.han.youtubespam.gateway.provider.JwtProvider;
import com.han.youtubespam.gateway.service.MemberService;
import com.han.youtubespam.gateway.type.GoogleTokenPair;
import com.han.youtubespam.gateway.type.OauthAttributePair;
import com.han.youtubespam.gateway.utils.TokenUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
	private final OAuth2AuthorizedClientService authorizedClientService;
	private final MemberService memberService;
	private final JwtProvider jwtProvider;
	private final ServerIPProperties ipProperties;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken)authentication;

		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
			auth.getAuthorizedClientRegistrationId(), auth.getName());
		// logOAuth2Auth(client);

		OAuth2User user = auth.getPrincipal();
		OauthAttributePair attrPair = new OauthAttributePair(user.getAttribute("sub"), user.getAttribute("email"));

		GoogleTokenPair googleTokenPair = new GoogleTokenPair(
			client.getAccessToken(),
			client.getRefreshToken() == null ? null : client.getRefreshToken()
		);
		boolean hasYoutubeAccess = googleTokenPair.accessToken().getScopes().contains(GoogleConstant.YOUTUBE_SCOPE);
		MemberEntity memberEntity = memberService.getOrSignup(attrPair, googleTokenPair, hasYoutubeAccess);
		log.info("$youtube access? {}", hasYoutubeAccess);

		String tempToken = jwtProvider.issueRtTt(memberEntity.getUuid(), JwtConstant.JWT_TYPE_TEMP, new Date(),
			TimeConstant.EXP_MILLIS_MIN * 5);

		ResponseCookie cookie = TokenUtil.genResponseCookie(CookieConstant.OAUTH_NONCE_TOKEN, tempToken,
			Duration.ofMillis(TimeConstant.EXP_MILLIS_MIN * 5));

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		String from = (String)request.getSession()
			.getAttribute(FilterConstant.OAUTH_CONTEXT);

		if ("web".equals(from))
			response.sendRedirect(ipProperties.getFront() + "/after-login");

	}

	private void logOAuth2Auth(OAuth2AuthenticationToken auth) {
		if (auth == null) {
			log.info("OAuth2AuthenticationToken: null");
			return;
		}
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
			auth.getAuthorizedClientRegistrationId(), auth.getName());
		OAuth2User user = auth.getPrincipal();

		String accessToken = client.getAccessToken().getTokenValue();
		String refreshToken = client.getRefreshToken() == null ? "" : client.getRefreshToken().getTokenValue();

		log.info("""
				[OAuth2Authentication]
				- authorizedClientRegistrationId: {}
				- name: {}
				- authorities: {}
				- attributes: {}
				- accessToken: {}
				- refreshToken: {}
				""", auth.getAuthorizedClientRegistrationId(), auth.getName(),
			auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(), user.getAttributes(),
			accessToken, refreshToken);
	}

}
