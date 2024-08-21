package kr.co.doritos.repository;

import java.util.List;
import java.util.Optional;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.domain.Member;
import kr.co.doritos.domain.Message;

public interface ChatRoomRepository {

	List<ChatRoom> findAll();
	Optional<ChatRoom> findById(String id);
	List<ChatRoom> findAllByCreator(Member member);
	List<ChatRoom> findAllByMember(Member member);
	List<Member> findAccessMemberListById(String id);
	List<Message> findMessageListById(String id);
	boolean isExists(String id);
	boolean isExistsByAccessMemberList(List<Member> accessMemberList);
	ChatRoom create(ChatRoom chatRoom);
	ChatRoom update(ChatRoom chatRoom);
	void delete(ChatRoom chatRoom);
	int nextVal();
}
