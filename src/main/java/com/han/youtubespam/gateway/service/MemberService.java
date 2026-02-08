package com.han.youtubespam.gateway.service;

import org.springframework.stereotype.Service;

import com.han.youtubespam.gateway.consts.RedisConstant;
import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.repository.MemberRepository;
import com.han.youtubespam.gateway.type.OauthAttributePair;
import com.han.youtubespam.gateway.type.TokenPair;
import com.han.youtubespam.gateway.utils.AesCodec;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final AesCodec aesCodec;
	private final MemberRepository memberRepository;
	private final RedisService redisService;

	public MemberEntity signUp(String sub, String email, String googleRt) {
		String encryptedGoogleRt = aesCodec.encrypt(googleRt);
		MemberEntity memberEntity = MemberEntity.builder()
			.sub(sub)
			.email(email)
			.googleRt(encryptedGoogleRt)
			.build();

		return memberRepository.save(memberEntity);
	}

	public MemberEntity getOrSignup(OauthAttributePair attrPair, TokenPair tokenPair) {
		MemberEntity memberEntity = memberRepository.findBySub(attrPair.sub())
			.orElseGet(() -> signUp(attrPair.sub(), attrPair.email(), tokenPair.refreshToken()));

		redisService.set(RedisConstant.REDIS_GOOGLE_ACCESS_KEY + memberEntity.getUuid(), tokenPair.accessToken(), null);

		return memberEntity;
	}
}
