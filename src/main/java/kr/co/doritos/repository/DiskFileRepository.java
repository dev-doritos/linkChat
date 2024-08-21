package kr.co.doritos.repository;

import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.dto.FileDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Repository
@Slf4j
public class DiskFileRepository implements FileRepository {

	private static Map<String, FileDTO> fileMap;

	@Value("${service.upload-directory}")
	private String uploadDirectory;

	static {
		fileMap = new HashMap<>();
		log.trace("[DISK] Memory Load...");
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
		return fileMap.values().stream()
			.filter(e -> e.getSaveFileName().equals(saveName))
			.findFirst()
			.get();
	}

	@Override
	public FileDTO save(ChatRoom chatRoom, MultipartFile file, String fileId) {

		String usingUploadDirectory = uploadDirectory + chatRoom.getId();
		File saveDir = new File(usingUploadDirectory);

		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		String orgName = file.getOriginalFilename();
		String extend = orgName.substring(orgName.lastIndexOf(".") +1); // 확장자

		File saveFile = new File(usingUploadDirectory + "/" + UUID.randomUUID().toString().toString() + "." + extend);

		FileDTO fileDTO = new FileDTO();

		try {

			file.transferTo(saveFile);

			fileDTO.setId(fileId);
			fileDTO.setChatRoomId(chatRoom.getId());
			fileDTO.setFileName(file.getOriginalFilename());
			fileDTO.setExtend(extend.toLowerCase(Locale.ROOT));
			fileDTO.setContentType(file.getContentType());
			fileDTO.setBytes(null); // Memory VS Disk
			fileDTO.setSaveFileName(saveFile.getName());

			fileMap.put(fileId, fileDTO);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileDTO;
	}

	@Override
	public String getUploadRoot() {
		return uploadDirectory;
	}
}
