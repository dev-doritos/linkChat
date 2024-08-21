package kr.co.doritos.repository;

import kr.co.doritos.domain.Team;
import java.util.List;

public interface TeamRepository {

	public List<Team> findAll();
	public Team findById(String id);
	public Team save(Team team);
}
