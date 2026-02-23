package com.han.youtubespam.gateway.entity;

import java.util.UUID;

import com.han.youtubespam.gateway.type.ChannelDataPair;
import com.han.youtubespam.gateway.type.YoutubeChannelDataPair;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "members",
	indexes = {
		@Index(
			name = "idx_member_sub",
			columnList = "sub",
			unique = true
		)
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberEntity {
	@Id
	@GeneratedValue
	@Column(length = 36, updatable = false, nullable = false)
	private UUID uuid;

	@Column(nullable = false)
	private String sub;

	@Column
	private String email;

	@Column
	@Builder.Default
	private Boolean emailOptIn = false;

	@Column
	private String googleRt;

	@Column
	private String channelId;

	@Column
	private String channelHandler;

	@Column
	private String channelName;

	@Column
	private String playlistId;

	@Enumerated(EnumType.STRING)
	@Column
	@Builder.Default
	private MemberRole role = MemberRole.USER;

	@Column
	private String fcmToken;

	@Column
	@Builder.Default
	private boolean hasYoutubeAccess = false;

	public void setGoogleRt(String rt) {
		if (rt == null || rt.isBlank())
			throw new IllegalArgumentException("Google Refresh Token MUST NOT be BLANK");

		this.googleRt = rt;
	}

	public void updateChannelInfo(YoutubeChannelDataPair pair) {
		this.channelName = pair.channelName();
		this.channelHandler = pair.channelHandler();
	}

	public void setRole(MemberRole role) {
		this.role = role == null ? MemberRole.USER : role;
	}

	public void setChannelData(ChannelDataPair dataPair) {
		this.channelId = dataPair.channelId();
		this.channelHandler = dataPair.channelHandler();
		this.channelName = dataPair.channelName();
		this.playlistId = dataPair.playlistId();
	}

	public void setFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	public void setHasYoutubeAccess(boolean b) {
		this.hasYoutubeAccess = b;
	}

	public void setEmailOptIn(boolean b) {
		this.emailOptIn = b;
	}

	@PrePersist
	void prePersist() {
		if (emailOptIn == null)
			emailOptIn = false;
	}
}
