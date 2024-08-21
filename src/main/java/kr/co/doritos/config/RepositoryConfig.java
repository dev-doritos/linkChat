package kr.co.doritos.config;

import kr.co.doritos.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Repository Bean 등록
 * Repository Layer 내에서 @Repository 표기하지 않음.
 */
@Configuration
public class RepositoryConfig {

	@Bean
	public TeamRepository teamRepository() { return new MemoryTeamRepository(); }

	@Bean
	public MemberRepository memberRepository() { return new MemoryMemberRepository(); }

	@Bean
	public ChatRoomRepository chatRoomRepository() { return new MemoryChatRoomRepository(); }

	@Bean
	public LiveRepository liveRepository() { return new MemoryLiveRepository();	}
}
