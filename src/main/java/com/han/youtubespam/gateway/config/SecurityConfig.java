package com.han.youtubespam.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.han.youtubespam.gateway.consts.CookieConstant;
import com.han.youtubespam.gateway.filter.JwtAuthenticationFilter;
import com.han.youtubespam.gateway.filter.OAuthContextFilter;
import com.han.youtubespam.gateway.handler.OAuth2LoginFailureHandler;
import com.han.youtubespam.gateway.handler.OAuth2LoginSuccessHandler;
import com.han.youtubespam.gateway.resolver.JwtAuthorizationRequestResolver;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
	private final JwtAuthorizationRequestResolver oAuthRequestResolver;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuthContextFilter oAuthContextFilter;
	private final CorsConfigurationSource corsConfigurationSource;

	private final String[] IGNORE_LIST = {
		"/error", "/oauth/**", "/login/**", "/auth/refresh",
		"/auth/complete", "/_debug/**",
		"/youtube/trending",
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(logout -> logout.logoutUrl("/auth/signout")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID", CookieConstant.REFRESH_TOKEN)
				.clearAuthentication(true)
				.logoutSuccessHandler(
					(request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
			.httpBasic(AbstractHttpConfigurer::disable)
			.requestCache(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(oAuthContextFilter,
				OAuth2AuthorizationRequestRedirectFilter.class)
			.authorizeHttpRequests(
				auth ->
					auth
						.requestMatchers(IGNORE_LIST).permitAll()
						.anyRequest().authenticated()
			)
			.oauth2Login(oauth -> oauth
				.authorizationEndpoint(
					endpoint -> endpoint.authorizationRequestResolver(oAuthRequestResolver))
				.successHandler(oAuth2LoginSuccessHandler)
				.failureHandler(oAuth2LoginFailureHandler)
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				})
			);
		return http.build();
	}

}
