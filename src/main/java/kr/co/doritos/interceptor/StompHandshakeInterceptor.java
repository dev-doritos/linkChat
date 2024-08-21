package kr.co.doritos.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;

@Slf4j
public class StompHandshakeInterceptor implements HandshakeInterceptor {

	private String getIp(ServerHttpRequest request) {
		String ip = "";

		if(request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
			ip = servletRequest.getServletRequest().getRemoteAddr();
		}
		return ip;
	}

	private String getSessionId(String uri) {
		String sessionId = "";

		String [] uriArray = uri.split("/");
		try {
			sessionId = uriArray[uriArray.length - 2];
		} catch (Exception e) {} // ignore

		return sessionId;
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

		String ip = getIp(request);
		String sessionId = getSessionId(request.getURI().toString());

		log.trace("[WebSocket-HandShake > Before] : {}, sessionId={}", ip, sessionId);

		attributes.put("ip", ip);
		attributes.put("sessionId", sessionId);

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
}
