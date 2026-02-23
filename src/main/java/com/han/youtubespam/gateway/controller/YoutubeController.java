package com.han.youtubespam.gateway.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.service.GoogleTokenService;
import com.han.youtubespam.gateway.service.MemberService;
import com.han.youtubespam.gateway.type.YoutubeCommentDeleteDto;
import com.han.youtubespam.gateway.type.youtube_data.YoutubeCommentResponseDto;
import com.han.youtubespam.gateway.type.youtube_data.YoutubeMineVideoDto;
import com.han.youtubespam.gateway.type.youtube_data.YoutubeQuotaDto;
import com.han.youtubespam.gateway.type.youtube_data.YoutubeTrendingDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class YoutubeController {

	@Qualifier("youtubeClient")
	private final WebClient youtubeClient;

	private final GoogleTokenService googleTokenService;
	private final MemberService memberService;

	// quota 0 - 어차피 admin token으로 진행
	@GetMapping("/trending")
	public Map<String, YoutubeTrendingDto> getYoutubeTrending() {
		return youtubeClient.get()
			.uri("/trendings/video")
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<
				Map<String, YoutubeTrendingDto>>() {
			})
			.block();
	}

	// quota 2
	@GetMapping("/videos/mine")
	public YoutubeMineVideoDto getMineVideos(
		@AuthenticationPrincipal UUID memberId,
		@RequestParam Optional<String> next
	) {
		String nextPageToken = next.orElse("");
		String googleAt = String.valueOf(googleTokenService.getAccessToken(memberId));
		if (googleAt.isEmpty())
			return null;

		MemberEntity member = memberService.getMember(memberId);

		MultiValueMap<String, String> queries = new LinkedMultiValueMap<>();
		queries.add("next", nextPageToken);
		queries.add("pid", member.getPlaylistId());
		return youtubeClient.get()
			.uri(builder -> builder
				.path("/video/mine")
				.queryParams(queries)
				.build()
			)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + googleAt)
			.retrieve()
			.bodyToMono(YoutubeMineVideoDto.class)
			.block();
	}

	// quota - 0 ~ 100
	@GetMapping("/comments")
	public YoutubeCommentResponseDto getCommentsInVideo(
		@RequestParam String vid
	) {
		YoutubeCommentResponseDto response = youtubeClient.get()
			.uri(builder -> builder
				.path("/comments/video")
				.queryParam("vid", vid)
				.build()
			)
			.retrieve()
			.bodyToMono(YoutubeCommentResponseDto.class)
			.block();
		response.getQuota();
		return response;
	}

	// quota - 50 * m
	@DeleteMapping("/comments")
	public int updateCommentsModeration(@AuthenticationPrincipal UUID memberId,
		@RequestBody YoutubeCommentDeleteDto body) {
		String googleAt = String.valueOf(googleTokenService.getAccessToken(memberId));
		YoutubeQuotaDto quotaDto = youtubeClient.method(HttpMethod.DELETE)
			.uri("/comments")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + googleAt)
			.bodyValue(body)
			.retrieve()
			.bodyToMono(YoutubeQuotaDto.class)
			.block();
		int quota = quotaDto.getQuota();

		return body.getCommentIds().length / 50;
	}
}
