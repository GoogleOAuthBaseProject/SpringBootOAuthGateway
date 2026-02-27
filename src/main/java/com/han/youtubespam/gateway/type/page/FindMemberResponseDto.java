package com.han.youtubespam.gateway.type.page;

import java.util.UUID;

import com.han.youtubespam.gateway.entity.MemberRole;

public record FindMemberResponseDto(
	UUID uuid,
	String handler,
	String channel,
	MemberRole role
) {
}
