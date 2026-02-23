package com.han.youtubespam.gateway.type.page;

public record FindMemberRequestDto(
	MemberSearchType type,
	String term
) {
}
