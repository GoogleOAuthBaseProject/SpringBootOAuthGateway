package com.han.youtubespam.gateway.type;

import com.han.youtubespam.gateway.type.youtube_data.YoutubeChannelDto;

public record ChannelDataPair(
	String channelId,
	String channelHandler,
	String channelName,
	String playlistId
) {
	public ChannelDataPair(YoutubeChannelDto channelDto) {
		this(channelDto.id(), channelDto.customUrl(), channelDto.title(), channelDto.playlistId());
	}
}
