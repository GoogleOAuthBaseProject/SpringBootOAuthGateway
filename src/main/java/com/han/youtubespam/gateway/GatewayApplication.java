package com.han.youtubespam.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
	// exclude = {
	// 		DataSourceAutoConfiguration.class,
	// 		HibernateJpaAutoConfiguration.class
	// }
)
@EnableScheduling
@EnableJpaAuditing
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
