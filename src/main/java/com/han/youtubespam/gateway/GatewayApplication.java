package com.han.youtubespam.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
	// exclude = {
	// 		DataSourceAutoConfiguration.class,
	// 		HibernateJpaAutoConfiguration.class
	// }
)
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
