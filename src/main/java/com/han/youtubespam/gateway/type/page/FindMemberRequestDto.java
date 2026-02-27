package com.han.youtubespam.gateway.type.page;

public record FindMemberRequestDto(
	MemberSearchType type,
	String term
) {
	public boolean isEmpty() {
		return term == null || term.isEmpty();
	}
}
