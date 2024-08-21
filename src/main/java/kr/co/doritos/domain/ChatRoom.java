package kr.co.doritos.domain;

import java.util.ArrayList;
import java.util.List;

import kr.co.doritos.common.AccessType;
import kr.co.doritos.common.UseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatRoom implements Cloneable {

	private String id; // topic-id : 채팅방 아이디
	private Member creator;
	private String logViewYn;
	private AccessType accessType;
	private String auth_ymd;
	private String auth_hms;
	private String name;
	private UseStatus status;
	private List<Member> accessMemberList = new ArrayList<>();  // 참여가능 회원 목록
	private List<Message> messageList = new ArrayList<>();      // 대화리스트
	private List<Member> liveMemberList = new ArrayList<>();    // 참여중인 회원 목록

	@Override
	public ChatRoom clone() {
		try {
			return (ChatRoom) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean instanceOf = obj instanceof ChatRoom;

		if (!instanceOf) {
			return false;
		}

		ChatRoom obj_ = (ChatRoom) obj;

		if (!this.id.equals(obj_.getId())) {
			return false;
		}

		return true;
	}
}