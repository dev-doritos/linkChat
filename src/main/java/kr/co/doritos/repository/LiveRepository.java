package kr.co.doritos.repository;

import kr.co.doritos.dto.LiveDTO;

public interface LiveRepository {

	public LiveDTO findBySessionId(String sessionId);
	public LiveDTO save(String sessionId, LiveDTO liveDTO);
	public void deleteBySessionId(String sessionId);
}
