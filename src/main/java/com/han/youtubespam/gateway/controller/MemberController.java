package com.han.youtubespam.gateway.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.han.youtubespam.gateway.consts.CookieConstant;
import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.service.MemberService;
import com.han.youtubespam.gateway.type.member.MemberMeResponse;
import com.han.youtubespam.gateway.utils.TokenUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/me")
	public ResponseEntity<MemberMeResponse> getMyInfo(
		Authentication authentication
	) {
		UUID memberId = (UUID)authentication.getPrincipal();
		log.info("{}", memberId);
		MemberEntity member = memberService.getMember(memberId);

		MemberMeResponse responseDto = new MemberMeResponse(member);
		return ResponseEntity.ok(responseDto);
	}

	@PostMapping("/signout")
	public ResponseEntity<?> logout() {
		ResponseCookie removeRefreshToken = TokenUtil.revokeResponseCookie(CookieConstant.REFRESH_TOKEN);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, removeRefreshToken.toString());
		return ResponseEntity.noContent().headers(headers).build();
	}

	@DeleteMapping("/withdraw")
	public ResponseEntity<?> withdraw(
		Authentication authentication
	) {
		ResponseCookie removeRefreshToken = TokenUtil.revokeResponseCookie(CookieConstant.REFRESH_TOKEN);
		memberService.withdrawal((UUID)authentication.getPrincipal());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, removeRefreshToken.toString());
		return ResponseEntity.noContent().headers(headers).build();
	}
}
