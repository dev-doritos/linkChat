package kr.co.doritos.service;

import java.util.ArrayList;
import java.util.List;

import kr.co.doritos.common.AccessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.domain.Member;
import kr.co.doritos.exception.ChatException;
import kr.co.doritos.repository.MemberRepository;

@Service
public class MemberService {

	private final MemberRepository memberRepository;

	private final String LOCAL_HOST = "127.0.0.1";
	private final String MY_IP = "0:0:0:0:0:0:0:1";

	@Autowired
	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public List<Member> findAll() {
		return memberRepository.findAll();
	}

	public boolean isLocalHost(String ip) {
		return LOCAL_HOST.equals(ip);
	}

	public String changeHostIp(String ip) {
		if (isLocalHost(ip)) {
			ip = MY_IP;
		}
		return ip;
	}

	public Member findByIp(String ip) {
		if (LOCAL_HOST.equals(ip)) {
			ip = MY_IP;
		}

		Member member = memberRepository.findByIp(ip)
				.orElseThrow(() -> new ChatException(ResponseCode.M9991));

		return member;
	}

	public Member findByName(String name) {
		Member member = memberRepository.findByName(name)
				.orElseThrow(() -> new ChatException(ResponseCode.M9991));
		;

		return member;
	}

	public List<Member> findByAccessTypeAndMemberIpArray(AccessType accessType, List<String> memberIps) {

		List<Member> accessMemberList = new ArrayList<>();

		switch (accessType) {
			case PROTECTED :
				for (String memberIp : memberIps) {
					accessMemberList.add(findByIp(memberIp));
				}
				break;

			default :
				accessMemberList = findAll();
		}

		return accessMemberList;
	}

	public Member save(Member member) {
		return memberRepository.save(member);
	}
}
