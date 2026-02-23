package com.han.youtubespam.gateway.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class GoogleClientConfig {
	@Bean
	@Qualifier("googleClient")
	public WebClient googleClient(WebClient.Builder builder) {
		HttpClient client = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
			.responseTimeout(Duration.ofMinutes(10))
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(5))
					.addHandlerLast(new WriteTimeoutHandler(5))
			);
		return builder
			.baseUrl("https://oauth2.googleapis.com")
			.clientConnector(
				new ReactorClientHttpConnector(client)
			).build();
	}
}
