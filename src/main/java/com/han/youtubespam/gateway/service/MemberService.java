package com.han.youtubespam.gateway.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.entity.MemberRole;
import com.han.youtubespam.gateway.repository.MemberRepository;
import com.han.youtubespam.gateway.repository.MemberSpecification;
import com.han.youtubespam.gateway.type.GoogleTokenPair;
import com.han.youtubespam.gateway.type.OauthAttributePair;
import com.han.youtubespam.gateway.type.YoutubeChannelDataPair;
import com.han.youtubespam.gateway.type.page.FindMemberRequestDto;
import com.han.youtubespam.gateway.type.page.FindMemberResponseDto;
import com.han.youtubespam.gateway.type.youtube_data.YoutubeChannelDto;
import com.han.youtubespam.gateway.utils.AesCodec;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
	private final AesCodec aesCodec;
	private final MemberRepository memberRepository;

	private final RedisService redisService;
	private final GoogleTokenService googleTokenService;

	@PersistenceContext
	private final EntityManager entityManager;

	@Qualifier("youtubeClient")
	private final WebClient youtubeClient;

	public MemberEntity signUp(String sub, String email, String googleRt) {
		String encryptedGoogleRt = aesCodec.encrypt(googleRt);
		MemberEntity memberEntity = MemberEntity.builder()
			.sub(sub)
			.email(email)
			.googleRt(encryptedGoogleRt)
			.build();

		return memberRepository.save(memberEntity);
	}

	public Page<FindMemberResponseDto> getMembersPagenated(Pageable pageable, FindMemberRequestDto requestDto) {
		return memberRepository.findAll(
				MemberSpecification.search(requestDto),
				pageable
			)
			.map(member -> new FindMemberResponseDto(member.getUuid(), member.getChannelHandler(),
				member.getChannelName()));
	}

	@Transactional
	public MemberEntity getOrSignup(OauthAttributePair attrPair, GoogleTokenPair JWTTokenPair, boolean hasAccess) {
		MemberEntity memberEntity = memberRepository.findBySub(attrPair.sub())
			.orElseGet(() -> signUp(attrPair.sub(), attrPair.email(), JWTTokenPair.refreshToken().getTokenValue()));
		memberEntity.setHasYoutubeAccess(hasAccess);
		googleTokenService.setAccessToken(memberEntity.getUuid(), JWTTokenPair.accessToken().getTokenValue());

		return memberEntity;
	}

	@Transactional
	public void syncMember() {
		int page = 0;

		Page<MemberEntity> result;

		do {
			result = memberRepository.findAll(
				PageRequest.of(page, 50)
			);

			List<MemberEntity> memberEntities = result.getContent();

			String channelIds = memberEntities.stream()
				.map(MemberEntity::getChannelId)
				.collect(Collectors.joining(","));
			Map<String, YoutubeChannelDto> channelInfosMap = youtubeClient.get()
				.uri("/channels", builder -> builder.queryParam("channelIds", channelIds).build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<
					Map<String, YoutubeChannelDto>>() {
				})
				.block();

			memberEntities.forEach(member -> {
				YoutubeChannelDto channelDto = channelInfosMap.get(member.getChannelId());
				YoutubeChannelDataPair pair = new YoutubeChannelDataPair(channelDto.title(),
					channelDto.customUrl());
				member.updateChannelInfo(pair);
			});

			entityManager.flush();
			entityManager.clear();

			page++;
		} while (result.hasNext());
	}

	@Transactional
	public void updateRole(UUID uuid, MemberRole role) {
		MemberEntity member = memberRepository.findByUuid(uuid)
			.orElseThrow(() -> new RuntimeException("Member not found"));
		member.setRole(role);
	}

	@Transactional
	public void withdrawal(UUID uuid) {
		MemberEntity member = getMember(uuid);
		String googleRefreshToken = aesCodec.decrypt(member.getGoogleRt());
		memberRepository.delete(member);

		googleTokenService.revokeGoogleToken(googleRefreshToken);
	}

	public MemberEntity getMember(UUID uuid) {
		return memberRepository.findByUuid(uuid)
			.orElseThrow(() -> new RuntimeException("Member not found"));
	}
}
