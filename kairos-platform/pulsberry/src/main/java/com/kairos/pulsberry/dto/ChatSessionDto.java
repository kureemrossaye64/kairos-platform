package com.kairos.pulsberry.dto;

import com.kairos.pulsberry.entity.ChatSession;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionDto extends BaseDto {

	private String userId;

	private String title;
	
	
	public static ChatSessionDto from(ChatSession session) {
		if(session == null) {
			return null;
		}
		ChatSessionDto r = new ChatSessionDto();
		r.setCreatedAt(session.getCreatedAt());
		r.setId(session.getId());
		r.setUpdatedAt(session.getUpdatedAt());
		r.setTitle(session.getTitle());
		r.setUserId(session.getUserId());
		return r;
	}

}
