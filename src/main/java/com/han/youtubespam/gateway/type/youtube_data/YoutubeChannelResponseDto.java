package com.han.youtubespam.gateway.type.youtube_data;

import java.util.List;

public record YoutubeChannelResponseDto(
	int quota,
	List<YoutubeChannelDto> data
) {
}
