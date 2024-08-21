package kr.co.doritos.interceptor;

import kr.co.doritos.service.ChatService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@AllArgsConstructor
@Slf4j
public class SessionListener {

	private final ChatService chatService;

	@EventListener
	public void onDisConnectEvent(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		chatService.doingLeave(sessionId);
	}
}
