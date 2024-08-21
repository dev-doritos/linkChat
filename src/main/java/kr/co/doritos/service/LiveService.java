package kr.co.doritos.service;

import kr.co.doritos.dto.LiveDTO;
import kr.co.doritos.repository.LiveRepository;
import org.springframework.stereotype.Service;

@Service
public class LiveService {

	private final LiveRepository liveRepository;

	public LiveService(LiveRepository liveRepository) {
		this.liveRepository = liveRepository;
	}

	public LiveDTO findBySessionId(String sessionId) {
		return liveRepository.findBySessionId(sessionId);
	}

	public LiveDTO save(String sessionId, LiveDTO liveDTO) {
		return liveRepository.save(sessionId, liveDTO);
	}

	public void deleteBySessionId(String sessionId) {
		liveRepository.deleteBySessionId(sessionId);
	}
}
