package kr.co.doritos.daemon;

import kr.co.doritos.common.UseStatus;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Component
public class GarbageCollector implements Runnable {

	private final ChatRoomService chatRoomService;

	@Value("${service.upload-directory}")
	private String uploadDirectory;

	public GarbageCollector(ChatRoomService chatRoomService) {
		this.chatRoomService = chatRoomService;
	}

	@Override
	public void run() {
		runGarbageCollector();
	}

	public void runGarbageCollector() {
		System.err.println("GarbageCollector Running...");

		// status 가 'N'인 채팅방을 삭제한다.
		while (true) {
			List<ChatRoom> chatRoomList = chatRoomService.findAll();
			Iterator<ChatRoom> iterator = chatRoomList.iterator();
			while (iterator.hasNext()) {
				ChatRoom chatRoom = iterator.next();
				if (chatRoom.getStatus() == UseStatus.N) {
					System.err.println("GarbageCollector working... " + chatRoom);

					// 채팅방 삭제
					iterator.remove();

					// upload 파일 삭제
					removeFile(chatRoom);
				}
			}

			try {
				Thread.sleep(5 * 60 * 1000); // 5분 주기
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		System.err.println("GarbageCollector End...");
	}

	private void removeFile(ChatRoom chatRoom) {
		try {

			String targetPath = uploadDirectory + chatRoom.getId();

			File target = new File(targetPath);

			System.err.println("GarbageCollector working upload... " + target.getName());

			if (!target.exists()) {
				return;
			}

			File [] files = target.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					file.delete();
				} else {
					Arrays.stream(file.listFiles()).forEach(e -> e.delete());
					file.delete();
				}
			}
			target.delete();

		} catch (Exception e) {} // ignore
	}
}
