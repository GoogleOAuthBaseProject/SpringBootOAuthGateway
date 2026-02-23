package com.han.youtubespam.gateway.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.ip")
@Getter
@Setter
public class ServerIPProperties {
	private String crawler;
	private String predict;
	private String front;
}
