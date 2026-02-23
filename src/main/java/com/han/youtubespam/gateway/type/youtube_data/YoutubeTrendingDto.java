package com.han.youtubespam.gateway.type.youtube_data;

import java.util.List;

import lombok.Data;

@Data
public class YoutubeTrendingDto {
	private String name;
	private List<YoutubeTrendingItemDto> items;
}

@Data
class YoutubeTrendingItemDto {
	private YoutubeVideoDto video;
	private YoutubeChannelDto channel;
}
