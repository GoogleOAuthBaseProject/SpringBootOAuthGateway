package com.han.youtubespam.gateway.filter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.han.youtubespam.gateway.consts.JwtConstant;
import com.han.youtubespam.gateway.provider.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String token = resolveAccessToken(request);
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		boolean isValid = jwtProvider.validate(token, JwtConstant.JWT_TYPE_ACCESS);
		UUID userId = jwtProvider.getUserId(token);
		if (!isValid || userId == null) {
			response.setStatus(401);
			response.setHeader("X-Auth-Error", "INVALID_TOKEN");
			filterChain.doFilter(request, response);
			return;
		}

		Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
		SecurityContextHolder.getContext().setAuthentication(auth);

		filterChain.doFilter(request, response);
	}

	String resolveAccessToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
	}
}
