package kr.co.doritos.repository;

import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.dto.FileDTO;
import kr.co.doritos.exception.ChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MemoryFileRepository implements FileRepository {

	private final Map<String, FileDTO> fileMap;

	public MemoryFileRepository() {
		fileMap = new HashMap<>();
		log.trace("[FILE] Memory File-Data Load... {}", fileMap);
	}

	@Override
	public Map<String, FileDTO> findAll() {
		return fileMap;
	}

	@Override
	public FileDTO findById(String id) {
		return fileMap.get(id);
	}

	@Override
	public FileDTO findBySaveName(String saveName) {
		return null;
	}

	@Override
	public FileDTO save(ChatRoom chatRoom, MultipartFile file, String fileId) {

		FileDTO fileDTO = new FileDTO();

		try {

			fileDTO.setId(fileId);
			fileDTO.setChatRoomId(chatRoom.getId());
			fileDTO.setFileName(file.getOriginalFilename());
			fileDTO.setBytes(file.getBytes());
			fileDTO.setContentType(file.getContentType());

			fileMap.put(fileId, fileDTO);

			return fileDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ChatException(ResponseCode.F9991);
		}
	}

	@Override
	public String getUploadRoot() {
		return null;
	}
}