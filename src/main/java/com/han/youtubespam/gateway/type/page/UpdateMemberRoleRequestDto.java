package com.han.youtubespam.gateway.type.page;

import com.han.youtubespam.gateway.entity.MemberRole;

import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequestDto(
	@NotNull MemberRole role
) {
}
