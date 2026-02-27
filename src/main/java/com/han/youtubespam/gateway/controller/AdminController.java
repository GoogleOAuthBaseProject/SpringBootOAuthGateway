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
import com.han.youtubespam.gateway.type.member.MemberRoleUpdateRequest;
import com.han.youtubespam.gateway.type.page.FindMemberRequestDto;
import com.han.youtubespam.gateway.type.page.FindMemberResponseDto;
import com.han.youtubespam.gateway.type.page.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@DeleteMapping("/members/{uuid}")
	public ResponseEntity<Void> forceWithdraw(@PathVariable("uuid") UUID uuid) {
		memberService.withdrawal(uuid);
		messagingService.send(uuid, SseConstant.SSE_MEMBER_UPDATE);

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/members/{uuid}/role")
	public ResponseEntity<Void> updateMember(@PathVariable("uuid") UUID uuid,
		@Valid @RequestBody MemberRoleUpdateRequest request) {
		memberService.updateRole(uuid, request.role());
		messagingService.send(uuid, SseConstant.SSE_MEMBER_UPDATE);

		return ResponseEntity.noContent().build();
	}
}
