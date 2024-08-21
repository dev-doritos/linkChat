package kr.co.doritos.repository;

import kr.co.doritos.dto.LiveDTO;
import java.util.HashMap;
import java.util.Map;

public class MemoryLiveRepository implements LiveRepository {

	private final Map<String, LiveDTO> liveInfoHm;

	public MemoryLiveRepository() {
		liveInfoHm = new HashMap<>();
	}

	@Override
	public LiveDTO findBySessionId(String sessionId) {
		return liveInfoHm.get(sessionId);
	}

	@Override
	public LiveDTO save(String sessionId, LiveDTO liveDTO) {
		liveInfoHm.put(sessionId, liveDTO);
		return liveDTO;
	}

	@Override
	public void deleteBySessionId(String sessionId) {
		liveInfoHm.remove(sessionId);
	}
}
