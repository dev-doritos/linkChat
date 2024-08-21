package kr.co.doritos.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.co.doritos.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryMemberRepository implements MemberRepository {

	private final List<Member> memberList;

	public MemoryMemberRepository() {
		memberList = new ArrayList<>();
	}

	@Override
	public List<Member> findAll() {
		return (List<Member>) ((ArrayList) memberList).clone();
	}

	@Override
	public Optional<Member> findByIp(String ip) {
		return memberList.stream()
			.filter(m -> m.getIp().equals(ip))
			.findAny();
	}

	@Override
	public Optional<Member> findByName(String name) {
		return memberList.stream()
			.filter(m -> m.getName().equals(name))
			.findAny();
	}

	@Override
	public Member save(Member member) {
		memberList.add(member);
		return member;
	}
}
