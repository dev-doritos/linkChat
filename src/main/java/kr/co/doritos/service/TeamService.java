package kr.co.doritos.service;

import kr.co.doritos.domain.Team;
import kr.co.doritos.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeamService {

	private final TeamRepository teamRepository;

	@Autowired
	public TeamService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	public List<Team> findAll() {
		return teamRepository.findAll();
	}

	public Team findById(String id) {
		return teamRepository.findById(id);
	}

	public Team save(Team team) { return teamRepository.save(team); }
}
