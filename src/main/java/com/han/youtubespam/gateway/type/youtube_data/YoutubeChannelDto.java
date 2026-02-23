package com.han.youtubespam.gateway.type.youtube_data;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record YoutubeChannelDto(
	String id, // channelId;
	String title, // channelName;
	String customUrl, //  handler
	YoutubeThumbnailDto thumbnail,
	String playlistId) {
}

