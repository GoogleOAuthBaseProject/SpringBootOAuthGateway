package com.han.youtubespam.gateway.type.member;

import java.util.UUID;

import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.entity.MemberRole;

public record MemberMeResponse(
	UUID id,
	Boolean emailOptIn,
	Boolean hasYoutubeAccess,
	String channelId,
	String channelName,
	String channelHandler,
	String playlistId,
	MemberRole role
) {
	public MemberMeResponse(MemberEntity member) {
		this(member.getUuid(), member.getEmailOptIn(), member.isHasYoutubeAccess(), member.getChannelId(),
			member.getChannelName(), member.getChannelHandler(), member.getPlaylistId(), member.getRole());
	}
}
