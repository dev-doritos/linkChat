package kr.co.doritos.domain;

import kr.co.doritos.common.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessage {

	private ChannelType type;
	private String chatRoomId;
	private String message;
	private String senderIp;
}
