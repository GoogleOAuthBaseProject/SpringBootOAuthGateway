package com.han.youtubespam.gateway.interceptor;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.han.youtubespam.gateway.service.GoogleTokenService;
import com.han.youtubespam.gateway.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleAccessTokenInterceptor implements HandlerInterceptor {
	private final GoogleTokenService googleTokenService;
	private final MemberService memberService;

	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null)
			return true;

		String method = request.getMethod();
		String uri = request.getRequestURI();

		if (
			("GET".equals(method) || "/videos/mine".equals(uri)) ||
				("DELETE".equals(method) && "/comments".equals(uri))
		) {
			UUID memberId = (UUID)authentication.getPrincipal();
			String accessToken = googleTokenService.getAccessToken(memberId);
			if (accessToken.isBlank())
				googleTokenService.renewGoogleAccessToken(memberService.getMember(memberId));
		}

		return true;
	}
}
