package kr.co.doritos.service;

import kr.co.doritos.common.ChannelType;
import kr.co.doritos.domain.ChatMessage;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.domain.Member;
import kr.co.doritos.domain.Message;
import kr.co.doritos.dto.LiveDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private final LiveService liveService;

	@Autowired
	public ChatService(SimpMessagingTemplate simpMessagingTemplate, LiveService liveService) {
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.liveService = liveService;
	}

	public Message chatMessageToMessage(ChatMessage chatMessage, Member member) {
		Message message = new Message();
		message.setType(chatMessage.getType());
		message.setChatRoomId(chatMessage.getChatRoomId());
		message.setSender(member);
		message.setMessage(chatMessage.getMessage());
		return message;
	}

	public void receiveMessage(ChatRoom chatRoom, Message message) {
		// Message 저장
		chatRoom.getMessageList().add(message);

		// 채널분기
		switch (message.getType()) {
			case JOIN:
				doingJoin(chatRoom, message);
				break;
			case LEAVE:
				doingLeave(chatRoom, message);
				break;
			case MESSAGE:
				doingMessage(chatRoom, message);
				break;
		}
	}

	private void doingJoin(ChatRoom chatRoom, Message message) {

		log.trace("[{}][{}] 채팅방 Join.", message.getSender().getIp(), chatRoom.getId());

		Member member = message.getSender();

		// LiveDTO 저장
		LiveDTO liveDTO = new LiveDTO();
		liveDTO.setChatRoom(chatRoom);
		liveDTO.setMember(member);
		liveService.save(message.getSessionId(), liveDTO);

		// LiveMemberList 추가
		chatRoom.getLiveMemberList().add(member);

		// 상태전달
		ChatRoom cloneRoom = chatRoom.clone();
		cloneRoom.setMessageList(null); // 대화목록 보내지 않음.
		cloneRoom.setLiveMemberList(distinctLiveMemberList(chatRoom));
		convertAndSendForRoomStatus(cloneRoom);

		// 처음 입장일 경우,
		int memberSize;
		memberSize = (int) chatRoom.getLiveMemberList().stream().filter(e -> e.getIp().equals(member.getIp())).count();

		if (memberSize == 1) {
			message.setType(ChannelType.JOIN);
			message.setMessage(String.format("%s 님이 입장하셨습니다.", member.getName()));
			convertAndSendForMessage(chatRoom, message);
		}
	}

	public void doingLeave(String sessionId) {

		// LiveDTO 제거
		LiveDTO liveDTO = liveService.findBySessionId(sessionId);
		liveService.deleteBySessionId(sessionId);

		// Message 생성
		Message message = new Message();
		message.setSessionId(sessionId);
		message.setType(ChannelType.LEAVE);
		message.setMessage("");
		message.setChatRoomId(liveDTO.getChatRoom().getId());
		message.setSender(liveDTO.getMember());

		doingLeave(liveDTO.getChatRoom(), message);
	}


	private void doingLeave(ChatRoom chatRoom, Message message) {

		log.trace("[{}][{}] 채팅방 Leave.", message.getSender().getIp(), chatRoom.getId());

		Member member = message.getSender();

		// Disconnected 회원 삭제
		Iterator<Member> iterator = chatRoom.getLiveMemberList().iterator();
		while (iterator.hasNext()) {
			Member m = iterator.next();
			if (m.getIp().equals(member.getIp())) {
				iterator.remove();
				break;
			}
		}

		ChatRoom cloneRoom = chatRoom.clone();
		cloneRoom.setMessageList(null); // 대화목록 보내지 않음.
		cloneRoom.setLiveMemberList(distinctLiveMemberList(chatRoom));
		convertAndSendForRoomStatus(cloneRoom);

		// 마지막 퇴장일 경우,
		int memberSize;
		memberSize = (int) chatRoom.getLiveMemberList().stream().filter(e -> e.getIp().equals(member.getIp())).count();

		if (memberSize == 0) {
			message.setType(ChannelType.LEAVE);
			message.setMessage(String.format("%s 님이 퇴장하셨습니다.", member.getName()));
			convertAndSendForMessage(chatRoom, message);
		}
	}

	private void doingMessage(ChatRoom chatRoom, Message message) {
		String messageStr = message.getMessage();
		if (messageStr.startsWith("http://") || messageStr.startsWith("https://")) {
			message.setType(ChannelType.LINK);
		}

		// 메시지 전송
		convertAndSendForMessage(chatRoom, message);
	}

	private List<Member> distinctLiveMemberList(ChatRoom chatRoom) {
		return chatRoom.getLiveMemberList().stream()
				.distinct()
				.collect(Collectors.toList());
	}

	private String getChatMessageDestination(ChatRoom chatRoom) {
		return String.format("/topic/chat/room/%s", chatRoom.getId());
	}

	private String getChatRoomStatusDestination(ChatRoom chatRoom) {
		return String.format("/topic/chat/room/%s/status", chatRoom.getId());
	}

	private Message converMessageForView(Message message) {
		// 시간 Format
		String auth_ymd = message.getAuth_ymd();
		String auth_hms = message.getAuth_hms();

		auth_ymd = String.format("%s/%s/%s", auth_ymd.substring(0, 4), auth_ymd.substring(4, 6), auth_ymd.substring(6, 8));
		auth_hms = String.format("%s:%s:%s", auth_hms.substring(0, 2), auth_hms.substring(2, 4), auth_hms.substring(4, 6));

		message.setAuth_ymd(auth_ymd);
		message.setAuth_hms(auth_hms);

		return message;
	}

	public void convertAndSendForRoomStatus(ChatRoom chatRoom) {
		simpMessagingTemplate.convertAndSend(getChatRoomStatusDestination(chatRoom), chatRoom);
	}

	public void convertAndSendForMessage(ChatRoom chatRoom, Message message) {
		simpMessagingTemplate.convertAndSend(getChatMessageDestination(chatRoom), converMessageForView(message));
	}
}
