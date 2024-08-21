package kr.co.doritos.controller;

import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.domain.Member;
import kr.co.doritos.service.ChatRoomService;
import kr.co.doritos.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatSseController {

    private final MemberService memberService;
    private final ChatRoomService chatRoomService;

    @GetMapping(value = "stream/chat/rooms")
    public void streamEvents(HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        Member member = memberService.findByIp(ip);

        log.trace("[SSE/CHAT/ROOM] {} 구독", ip);

        try {
            PrintWriter writer = response.getWriter();

            while (true) {
                // 입장 가능한 채팅방 조회
                List<ChatRoom> chatRoomList = chatRoomService.findAllByMember(member);

                StringBuilder builder = new StringBuilder();
                builder.append("[");
                boolean isFirst = true;
                for (ChatRoom chatRoom : chatRoomList) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        builder.append(",");
                    }
                    builder.append("{");
                    builder.append("\"chatRoomId\"").append(":").append("\"").append(chatRoom.getId()).append("\"");
                    builder.append(",").append("\"chatRoomName\"").append(":").append("\"").append(chatRoom.getName()).append("\"");
                    builder.append("}");
                }
                builder.append("]");

                log.trace(builder.toString());

                // 메시지 전송
                writer.write(String.format("data: %s\n\n", builder.toString()));
                writer.flush(); // 데이터 전송

                Thread.sleep(1000 * 10);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
