package com.han.youtubespam.gateway.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.han.youtubespam.gateway.consts.SseConstant;
import com.han.youtubespam.gateway.service.MemberService;
import com.han.youtubespam.gateway.service.MessagingService;
import com.han.youtubespam.gateway.type.page.FindMemberRequestDto;
import com.han.youtubespam.gateway.type.page.FindMemberResponseDto;
import com.han.youtubespam.gateway.type.page.PageResponse;
import com.han.youtubespam.gateway.type.page.UpdateMemberRoleRequestDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasRole('DEVELOPER')")
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final MemberService memberService;
	private final MessagingService messagingService;

	@GetMapping("/members")
	public ResponseEntity<PageResponse<FindMemberResponseDto>> getMembers(Pageable pageable,
		@ModelAttribute() FindMemberRequestDto findMemberDto) {
		Page<FindMemberResponseDto> page = memberService.getMembersPagenated(pageable, findMemberDto);

		return ResponseEntity.ok(PageResponse.from(page));
	}

	@Transactional
	@DeleteMapping("/withdraw/{uuid}")
	public void forceSingout(@PathVariable("uuid") UUID uuid) {
		memberService.withdrawal(uuid);
		messagingService.send(uuid, SseConstant.SSE_MEMBER_UPDATE);
	}

	// sse나 fcm 메시지 전달
	@PatchMapping("/role/{uuid}")
	public void updateRole(@PathVariable("uuid") UUID uuid, @RequestBody UpdateMemberRoleRequestDto body) {
		memberService.updateRole(uuid, body.role());
		// redis sse broadcast 후 모든 서버가 읽어 처리
		// 현재는 단일 서버니 sse는 여기서만 처리
		messagingService.send(uuid, SseConstant.SSE_MEMBER_UPDATE);
	}
}
