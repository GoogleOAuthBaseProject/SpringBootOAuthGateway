package com.han.youtubespam.gateway.type.youtube_data;

import java.util.List;

import lombok.Data;

@Data
public class YoutubeCommentResponseDto {
	private int quota;
	private List<YoutubeThreadDto> threads;
}

@Data
class YoutubeThreadDto {
	private YoutubeCommentDto top;
	private List<YoutubeCommentDto> replies;
	private int totalReplies;
}

@Data
class YoutubeCommentDto {
	private String id;
	private String parentId;
	private YoutubeAuthorDto author;
	private YoutubeCommentDateDto date;
	private String content;
}

@Data
class YoutubeAuthorDto {
	private String id;
	private String nickname;
	private String handler;
	private String image;
}

@Data
class YoutubeCommentDateDto {
	private String publish;
	private String update;
}
