package kr.co.doritos.repository;

import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileRepository {

	public Map<String, FileDTO> findAll();
	public FileDTO findById(String id);
	public FileDTO findBySaveName(String saveName);
	public FileDTO save(ChatRoom chatRoom, MultipartFile file, String fileId);
	public String getUploadRoot();
}
