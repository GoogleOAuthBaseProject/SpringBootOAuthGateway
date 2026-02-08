package com.han.youtubespam.gateway.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesCodec {
	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int IV_SIZE = 12;
	private static final int TAG_LENGTH = 128;

	private final SecretKey secretKey;

	public AesCodec(@Value("${app.security.secret.crypto}") String base64Key) {
		byte[] keyBytes = Base64.getDecoder().decode(base64Key);
		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

	public String encrypt(String plainText) {
		try {
			byte[] iv = new byte[IV_SIZE];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

			ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
			buffer.put(iv);
			buffer.put(encrypted);

			return Base64.getEncoder().encodeToString(buffer.array());
		} catch (Exception e) {
			throw new IllegalStateException("AES 암호화 실패", e);
		}
	}

	public String decrypt(String encryptedText) {
		try {
			byte[] decoded = Base64.getDecoder().decode(encryptedText);
			ByteBuffer buffer = ByteBuffer.wrap(decoded);

			byte[] iv = new byte[IV_SIZE];
			buffer.get(iv);

			byte[] cipherText = new byte[buffer.remaining()];
			buffer.get(cipherText);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(
				Cipher.DECRYPT_MODE,
				secretKey,
				new GCMParameterSpec(TAG_LENGTH, iv)
			);

			return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("AES 복호화 실패", e);
		}
	}
}
