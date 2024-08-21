package kr.co.doritos.repository;

import java.util.List;
import java.util.Optional;

import kr.co.doritos.domain.Member;

public interface MemberRepository {

	List<Member> findAll();
	Optional<Member> findByIp(String ip);
	Optional<Member> findByName(String name);
	Member save(Member member);
}
