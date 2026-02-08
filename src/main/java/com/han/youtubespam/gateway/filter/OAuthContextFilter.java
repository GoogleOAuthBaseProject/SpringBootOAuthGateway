package com.han.youtubespam.gateway.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.han.youtubespam.gateway.consts.FilterConstant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthContextFilter extends OncePerRequestFilter {
	private static final Set<String> ALLOWED_FROM =
		Set.of("web", "app");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		if (request.getRequestURI().startsWith("/oauth2/authorization/")) {
			String from = request.getParameter(FilterConstant.OAUTH_URI_QUERY);
			if (from != null && ALLOWED_FROM.contains(from)) {
				request.getSession(true).setAttribute(FilterConstant.OAUTH_CONTEXT, from);
			}
		}
		filterChain.doFilter(request, response);
	}
}
