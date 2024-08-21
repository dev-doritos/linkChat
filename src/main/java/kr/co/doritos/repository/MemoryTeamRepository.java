package kr.co.doritos.repository;

import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.domain.Team;
import kr.co.doritos.exception.ChatException;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MemoryTeamRepository implements TeamRepository {

	private final List<Team> teamList;

	public MemoryTeamRepository() {
		teamList = new ArrayList<>();
	}

	@Override
	public List<Team> findAll() {
		return teamList;
	}

	@Override
	public Team findById(String id) {
		return teamList.stream()
				.filter(e -> e.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new ChatException(ResponseCode.R9998));
	}

	@Override
	public Team save(Team team) {
		teamList.add(team);
		return team;
	}
}
