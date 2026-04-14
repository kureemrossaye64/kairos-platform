package com.kairos.pulsberry.dto;

import com.kairos.pulsberry.entity.ChatMessageEntity;

import dev.langchain4j.data.message.ChatMessageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageEntityDto extends BaseDto {

	private ChatSessionDto session;

	private ChatMessageType type;

	private String content;

	private long sequenceNumber;
	
	
	public static ChatMessageEntityDto from(ChatMessageEntity e) {
		if(e == null) {
			return null;
		}
		ChatMessageEntityDto r = new ChatMessageEntityDto();
		r.setContent(e.getContent());
		r.setCreatedAt(e.getCreatedAt());
		r.setId(e.getId());
		r.setSequenceNumber(e.getSequenceNumber());
		r.setSession(ChatSessionDto.from(e.getSession()));
		r.setType(e.getType());
		r.setUpdatedAt(e.getUpdatedAt());
		return r;
	}

}
