package com.han.youtubespam.gateway.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.han.youtubespam.gateway.service.MessagingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class SseController {
	private final MessagingService messagingService;

	@GetMapping("/connect")
	public SseEmitter connect(@AuthenticationPrincipal UUID memberId) {
		return messagingService.connect(memberId);
	}
}
