package kr.co.doritos.dto;

import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.domain.Member;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LiveDTO {

	private Member member;
	private ChatRoom chatRoom;
}
