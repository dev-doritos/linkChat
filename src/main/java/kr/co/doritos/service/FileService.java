package kr.co.doritos.service;

import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.dto.FileDTO;
import kr.co.doritos.exception.ChatException;
import kr.co.doritos.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

	private final FileRepository fileRepository;

	@Autowired
	public FileService(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	public Map<String, FileDTO> findAll() {
		return fileRepository.findAll();
	}

	public FileDTO findById(String id) {
		return fileRepository.findById(id);
	}

	public FileDTO save(ChatRoom chatRoom, MultipartFile file) {
		return fileRepository.save(chatRoom, file, generateFileId());
	}

	private String generateFileId() {
		return UUID.randomUUID().toString().toLowerCase().substring(0, 10);
	}

	public String getUploadRoot() {
		return fileRepository.getUploadRoot();
	}

	public void directoryClear() {
		String uploadDirectoryStr = getUploadRoot();

		File uploadDirectory = new File(uploadDirectoryStr);

		if (!uploadDirectory.exists() || !uploadDirectory.isDirectory()) {
			return;
		}

		if (uploadDirectory.exists()) {
			File [] files = uploadDirectory.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					file.delete();
				} else {
					Arrays.stream(file.listFiles()).forEach(e -> e.delete());
					file.delete();
				}
			}
		}

		System.err.println(uploadDirectoryStr + " clear...");
	}

	public void streamingFile(HttpServletResponse response, FileDTO fileDTO) {

		byte [] bytes = fileDTO.getBytes();

		// Memory VS Disk
		if (bytes == null) {
			String saveFileName = fileDTO.getSaveFileName();

			String filePath = fileRepository.getUploadRoot() + fileDTO.getChatRoomId() + "\\";

			File file = new File(filePath + saveFileName);

			log.trace("[{}] 파일 다운로드. 원본파일명:{}", filePath + saveFileName, fileDTO.getFileName());

			if (!file.exists()) {
				throw new ChatException(ResponseCode.F9992);
			}

			try {
				bytes = Files.readAllBytes(Paths.get(filePath + saveFileName));
			} catch (IOException e) {
				e.printStackTrace();
				throw new ChatException(ResponseCode.F9993);
			}
		}

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;fileName=\"" + URLEncoder.encode(fileDTO.getFileName(), StandardCharsets.UTF_8) + "\";");
		response.setHeader("Content-Transfer-Encoding", "binary");

		try {

			response.getOutputStream().write(bytes);
			response.getOutputStream().flush();
			response.getOutputStream().close();

			bytes = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
