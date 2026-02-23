package com.han.youtubespam.gateway.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.han.youtubespam.gateway.repository.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagingService {

	private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
	private final MemberRepository memberRepository;

	public SseEmitter connect(UUID memberId) {
		SseEmitter emitter = new SseEmitter(0L); // 무제한 session

		emitters.computeIfAbsent(memberId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		emitter.onCompletion(() -> remove(memberId, emitter));
		emitter.onTimeout(() -> remove(memberId, emitter));
		emitter.onError(e -> remove(memberId, emitter));

		return emitter;
	}

	@Transactional
	public void send(UUID memberId, String type) {
		List<SseEmitter> userEmitters = emitters.get(memberId);
		if (userEmitters == null)
			return;

		for (SseEmitter emitter : userEmitters) {
			try {
				emitter.send(SseEmitter.event()
					.name(type)
					.data(type));
			} catch (Exception e) {
				remove(memberId, emitter);
			}
		}

		memberRepository.findByUuid(memberId).ifPresent(member -> {
			String token = member.getFcmToken();
			if (token != null && !token.isBlank()) {
				try {
					Message message = Message.builder()
						.setToken(token)
						.putData("type", type)
						.build();

					String response = FirebaseMessaging.getInstance().send(message);
				} catch (FirebaseMessagingException e) {
					if ("UNREGISTERED".equals(e.getErrorCode())) {
						member.setFcmToken("");
					}
				}
			}
		});
	}

	private void remove(UUID memberId, SseEmitter emitter) {
		List<SseEmitter> userEmitters = emitters.get(memberId);
		if (userEmitters != null) {
			userEmitters.remove(emitter);
			if (userEmitters.isEmpty()) {
				emitters.remove(memberId);
			}
		}
	}

	@Scheduled(fixedRate = 60_000) // 1분마다
	public void heartbeat() {
		for (Map.Entry<UUID, List<SseEmitter>> entry : emitters.entrySet()) {
			UUID memberId = entry.getKey();
			List<SseEmitter> emitters = entry.getValue();

			for (SseEmitter emitter : emitters) {
				try {
					emitter.send(SseEmitter.event().comment("keep-alive"));
				} catch (Exception e) {
					remove(memberId, emitter);
				}
			}
		}
	}
}
