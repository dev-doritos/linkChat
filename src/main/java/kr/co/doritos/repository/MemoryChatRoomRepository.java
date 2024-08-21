package kr.co.doritos.repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import kr.co.doritos.common.AccessType;
import kr.co.doritos.domain.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryChatRoomRepository implements ChatRoomRepository {

	private final List<ChatRoom> chatRoomList;
	private final AtomicInteger chatSeq = new AtomicInteger(0);

	public MemoryChatRoomRepository() {
		chatRoomList = new ArrayList<>();
	}

	@Override
	public List<ChatRoom> findAll() {
		return chatRoomList;
	}

	@Override
	public List<ChatRoom> findAllByMember(Member member) {
		return chatRoomList.stream()
				.filter(e -> e.getAccessType() == AccessType.ANY || e.getAccessMemberList().contains(member))
				.collect(Collectors.toList());
	}

	@Override
	public List<ChatRoom> findAllByCreator(Member member) {
		return chatRoomList.stream()
				.filter(e -> e.getCreator().getIp().equals(member.getIp()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<ChatRoom> findById(String id) {
		return chatRoomList.stream()
				.filter(e -> e.getId().equals(id))
				.findAny();
	}

	@Override
	public List<Member> findAccessMemberListById(String id) {
		return chatRoomList.stream()
				.filter(e -> e.getId().equals(id))
				.findAny()
				.get()
				.getAccessMemberList();
	}

	@Override
	public List<Message> findMessageListById(String id) {
		return chatRoomList.stream()
				.filter(e -> e.getId().equals(id))
				.findAny()
				.get()
				.getMessageList();
	}

	@Override
	public boolean isExists(String id) {
		return findById(id).isPresent();
	}

	@Override
	public boolean isExistsByAccessMemberList(List<Member> accessMemberList) {
		return chatRoomList.stream()
				.anyMatch(e -> e.getAccessMemberList().equals(accessMemberList));
	}

	@Override
	public ChatRoom create(ChatRoom chatRoom) {
		chatRoomList.add(chatRoom);
		return chatRoom;
	}

	@Override
	public ChatRoom update(ChatRoom chatRoom) {
		for (int i = 0; i < chatRoomList.size(); i++) {
			if (chatRoomList.get(i).getId().equals(chatRoom.getId())) {
				chatRoomList.set(i, chatRoom);
				break;
			}
		}
		return chatRoom;
	}

	@Override
	public void delete(ChatRoom chatRoom) {
		Iterator<ChatRoom> iterator = chatRoomList.iterator();
		while (iterator.hasNext()) {
			ChatRoom target = iterator.next();
			if (target.equals(chatRoom)) {
				iterator.remove();
			}
		}
	}

	@Override
	public int nextVal() {
		return chatSeq.incrementAndGet();
	}
}
