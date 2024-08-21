package kr.co.doritos.dto;

import lombok.Data;
import lombok.ToString;

import java.io.File;

@Data
@ToString
public class FileDTO {

	private String id;
	private String chatRoomId;
	private String fileName;
	private String extend;
	private byte [] bytes;
	private String saveFileName;
	private String contentType;
}
