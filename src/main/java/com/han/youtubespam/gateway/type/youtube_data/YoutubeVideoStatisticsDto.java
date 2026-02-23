package com.han.youtubespam.gateway.type.youtube_data;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YoutubeVideoStatisticsDto {
	private long viewCount;
	private long likeCount;
	private Long dislikeCount;
	private long commentCount;
}
