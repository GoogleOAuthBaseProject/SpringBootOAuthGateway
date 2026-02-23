package com.han.youtubespam.gateway.cron;

import org.springframework.stereotype.Service;

import com.han.youtubespam.gateway.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YoutubeMemberSyncScheduler {
	private final MemberService memberService;
}
