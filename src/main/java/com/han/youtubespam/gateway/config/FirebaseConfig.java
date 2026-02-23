package com.han.youtubespam.gateway.config;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {
	@PostConstruct
	public void init() throws Exception {
		if (!FirebaseApp.getApps().isEmpty()) {
			return;
		}

		InputStream serviceAccount =
			new ClassPathResource("firebase-adminsdk-spampredict.json").getInputStream();

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(serviceAccount))
			.build();

		FirebaseApp.initializeApp(options);

	}
}
