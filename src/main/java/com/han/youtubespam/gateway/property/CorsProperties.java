package com.han.youtubespam.gateway.property;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.security.cors")
@Getter
@Setter
public class CorsProperties {
	private List<String> allowedOrigins;
}
