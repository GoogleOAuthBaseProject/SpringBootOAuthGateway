package com.han.youtubespam.gateway.type.youtube_data;

import lombok.Data;

@Data
public class YoutubeMineVideoDto {
	private String nextPageToken;
	private YoutubeVideoDto videos;
}


