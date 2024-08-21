package kr.co.doritos.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import kr.co.doritos.common.AccessType;
import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.common.UseStatus;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.domain.Member;
import kr.co.doritos.domain.Message;
import kr.co.doritos.exception.ChatException;
import kr.co.doritos.repository.ChatRoomRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;

	public ChatRoomService(ChatRoomRepository chatRoomRepository) {
		this.chatRoomRepository = chatRoomRepository;
	}

	public List<ChatRoom> findAll() {
		return chatRoomRepository.findAll().stream()
				.filter(e -> e.getStatus() == UseStatus.Y)
				.collect(Collectors.toList());
	}

	public List<ChatRoom> findAllByMember(Member member) {
		if (member == null) {
			throw new ChatException(ResponseCode.R9998);
		}

		return chatRoomRepository.findAllByMember(member).stream()
				.filter(e -> e.getStatus() == UseStatus.Y)
				.collect(Collectors.toList());
	}

	public List<ChatRoom> findAllByCreator(Member member) {
		if (member == null) {
			throw new ChatException(ResponseCode.R9998);
		}

		return chatRoomRepository.findAllByCreator(member).stream()
				.filter(e -> e.getStatus() == UseStatus.Y)
				.collect(Collectors.toList());
	}

	public ChatRoom findById(String id) {
		Optional<ChatRoom> opt = chatRoomRepository.findById(id).filter(e -> e.getStatus() == UseStatus.Y);
		if (opt.isEmpty()) {
			throw new ChatException(ResponseCode.C9991);
		}
		return opt.get();
	}

	public List<Member> findAccessMemberListById(String id) {
		if (!chatRoomRepository.isExists(id)) {
			return new ArrayList<Member>();
		}

		return chatRoomRepository.findAccessMemberListById(id);
	}

	public List<Message> findMessageListById(String id) {
		if (!chatRoomRepository.isExists(id)) {
			return new ArrayList<Message>();
		}

		return chatRoomRepository.findMessageListById(id);
	}

	public ChatRoom save(ChatRoom chatRoom) {
		if (chatRoomRepository.isExists(chatRoom.getId())) {
			throw new ChatException(ResponseCode.C9992);
		}

		/* 시스템에서 만든 채팅방의 경우에는 아이디 변경하지 않음. */
		if (!chatRoom.getCreator().getName().equals("SYSTEM")) {
			String usingId = getChatRoomId();
			chatRoom.setId(usingId);
		}

		log.trace("채팅방 생성정보 : {}", chatRoom);

		return chatRoomRepository.create(chatRoom);
	}

	public ChatRoom update(ChatRoom chatRoom) {
		if (!chatRoomRepository.isExists(chatRoom.getId())) {
			throw new ChatException(ResponseCode.C9991);
		}

		return chatRoomRepository.update(chatRoom);
	}

	public void delete(ChatRoom chatRoom) {
		if (!chatRoomRepository.isExists(chatRoom.getId())) {
			throw new ChatException(ResponseCode.C9991);
		}

		chatRoomRepository.delete(chatRoom);
	}

	public int nextVal() {
		return chatRoomRepository.nextVal();
	}

	public String getChatRoomId() {
		String id = "";
		id = UUID.randomUUID().toString().toLowerCase();
		return id;
	}

	public boolean isExistsByAccessMemberList(List<Member> accessMemberList) {
		return chatRoomRepository.isExistsByAccessMemberList(accessMemberList);
	}

	public boolean isPossibleJoin(ChatRoom chatRoom, Member member) {
		if (chatRoom.getAccessType() == AccessType.ANY) {
			return true;
		}

		return chatRoom.getAccessMemberList().contains(member);
	}

	public ChatRoom generateChatRoomByBodyMap(Member member, Map<String, Object> bodyMap, List<Member> accessMemberList) {

		// 채팅방 객체 생성
		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setCreator(member);
		chatRoom.setName((String) bodyMap.get("name"));
		chatRoom.setMessageList(new ArrayList<Message>());
		chatRoom.setAccessType(AccessType.findByCodeStr((String) bodyMap.get("accessType")));
		chatRoom.setStatus(String.valueOf(bodyMap.get("status")).equals("Y") ? UseStatus.Y : UseStatus.N);
		chatRoom.setAccessMemberList(accessMemberList);
		chatRoom.setLogViewYn((String) bodyMap.get("logViewYn"));
		chatRoom.setAuth_ymd(new SimpleDateFormat("yyyyMMdd").format(new Date()));
		chatRoom.setAuth_hms(new SimpleDateFormat("HHmmss").format(new Date()));

		return chatRoom;
	}

	public void convertChatRoomByBodyMap(ChatRoom chatRoom, Map<String, Object> bodyMap, List<Member> accessMemberList) {
		chatRoom.setName((String) bodyMap.get("name"));
		chatRoom.setStatus(String.valueOf(bodyMap.get("status")).equals("Y") ? UseStatus.Y : UseStatus.N);
		chatRoom.setLogViewYn(String.valueOf(bodyMap.get("status")).equals("Y") ? "Y" : "N");
		chatRoom.setAccessMemberList(accessMemberList);
	}

	public void addAccessMember(ChatRoom chatRoom, List<Member> members) {
		members.forEach(e -> chatRoom.getAccessMemberList().add(e));
	}

	public ChatRoom processChatRoom(ChatRoom chatRoom) {
		String logViewYn = chatRoom.getLogViewYn();

		// 깊은복사
		ChatRoom vChatRoom = chatRoom.clone();

		/// 대화목록 내보내기 확인
		if (!"Y".equals(logViewYn)) {
			vChatRoom.setMessageList(new ArrayList<Message>());
			return vChatRoom;
		}

		vChatRoom.setLiveMemberList(distinctLiveMemberList(vChatRoom));

		return vChatRoom;
	}

	public ChatRoom processChatRoomForStatus(ChatRoom chatRoom) {
		ChatRoom cloneRoom = chatRoom.clone();

		cloneRoom.setMessageList(null); // 대화목록 보내지 않음.
		cloneRoom.setLiveMemberList(distinctLiveMemberList(cloneRoom));

		return cloneRoom;
	}

	private List<Member> distinctLiveMemberList(ChatRoom chatRoom) {
		return chatRoom.getLiveMemberList().stream()
					.distinct()
					.collect(Collectors.toList());
	}

}