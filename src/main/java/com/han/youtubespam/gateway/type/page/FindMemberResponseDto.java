package com.han.youtubespam.gateway.type.page;

import java.util.UUID;

public record FindMemberResponseDto(
	UUID uuid,
	String channelHandler,
	String channelName
) {
}
