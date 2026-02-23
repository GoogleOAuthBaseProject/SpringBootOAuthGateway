package com.han.youtubespam.gateway.type.youtube_data;

import lombok.Data;

@Data
class YoutubeVideoDto {
	private String id;
	private String channelId;
	private String title;
	private YoutubeThumbnailDto thumbnail;
	private YoutubeVideoStatisticsDto statistics;
}
/*
    nextPageToken: string;
    videos: {
        id: string;
        channelId: string;
        title: string;
        thumbnail: {
            url: string;
            width: number;
            height: number;
        };
        statistics: {
            viewCount: number;
            likeCount: number;
            dislikeCount: number;
            commentCount: number;
        };
    }[];
 */
