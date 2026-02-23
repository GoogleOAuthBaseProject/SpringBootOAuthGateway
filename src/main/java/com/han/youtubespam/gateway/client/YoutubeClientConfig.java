package com.han.youtubespam.gateway.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.han.youtubespam.gateway.property.ServerIPProperties;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class YoutubeClientConfig {
	/*
	[ DNS ] → [ TCP CONNECT ] → [ REQUEST WRITE ] → [ SERVER PROCESS ] → [ RESPONSE READ ]
             ↑               ↑                    ↑                    ↑
        connectTimeout   writeTimeout        responseTimeout        readTimeout
	 */
	@Bean
	@Qualifier("youtubeClient")
	public WebClient youtubeClient(WebClient.Builder builder, ServerIPProperties ipProperties) {
		HttpClient client = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
			.responseTimeout(Duration.ofSeconds(20))
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(10))
					.addHandlerLast(new WriteTimeoutHandler(5))
			);
		return builder
			.baseUrl(ipProperties.getCrawler())
			.clientConnector(
				new ReactorClientHttpConnector(client)
			).build();
	}
}
