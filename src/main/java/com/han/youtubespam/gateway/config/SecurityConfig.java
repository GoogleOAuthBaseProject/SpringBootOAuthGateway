package com.han.youtubespam.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.han.youtubespam.gateway.filter.JwtAuthenticationFilter;
import com.han.youtubespam.gateway.filter.OAuthContextFilter;
import com.han.youtubespam.gateway.handler.OAuth2LoginSuccessHandler;
import com.han.youtubespam.gateway.resolver.JwtAuthorizationRequestResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final JwtAuthorizationRequestResolver oAuthRequestResolver;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuthContextFilter oAuthContextFilter;
	private final CorsConfigurationSource corsConfigurationSource;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.requestCache(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(oAuthContextFilter,
				OAuth2AuthorizationRequestRedirectFilter.class)
			.authorizeHttpRequests(
				auth -> auth.requestMatchers("/oauth/**", "/login/**", "/auth/refresh", "/auth/complete", "/_debug/**")
					.permitAll()
					.anyRequest()
					.authenticated())
			.oauth2Login(oauth -> oauth
				.authorizationEndpoint(
					endpoint -> endpoint.authorizationRequestResolver(oAuthRequestResolver))
				.successHandler(oAuth2LoginSuccessHandler)
				.failureHandler((request, response, exception) -> {
					System.out.println("❌ OAuth 로그인 실패");
					exception.printStackTrace();
				})
			);
		return http.build();
	}

}
