package kr.co.doritos.controller;

import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.co.doritos.common.AccessType;
import kr.co.doritos.common.ChannelType;
import kr.co.doritos.common.UseStatus;
import kr.co.doritos.domain.ChatMessage;
import kr.co.doritos.domain.Message;
import kr.co.doritos.dto.FileDTO;
import kr.co.doritos.service.ChatService;
import kr.co.doritos.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.domain.ChatRoom;
import kr.co.doritos.domain.Member;
import kr.co.doritos.exception.ChatException;
import kr.co.doritos.service.ChatRoomService;
import kr.co.doritos.service.MemberService;
import lombok.extern.slf4j.Slf4j;

/**
 * ChatController
 * 채팅방 requestMapping
 */
@Controller
@Slf4j
public class ChatController {

	private final MemberService memberService;
	private final ChatRoomService chatRoomService;
	private final ChatService chatService;
	private final FileService fileService;

	@Value("${service.images}")
	private String serviceImages;

	@Autowired
	public ChatController(MemberService memberService, ChatRoomService chatRoomService, ChatService chatService, FileService fileService) {
		this.memberService = memberService;
		this.chatRoomService = chatRoomService;
		this.chatService = chatService;
		this.fileService = fileService;
	}

	/**
	 * [채팅방 대기실]
	 * 입장 가능한 채팅방 조회
	 */
	@GetMapping(value="/chat/room")
	public ModelAndView toWaitRoom(HttpServletRequest request) {
		
		String ip = request.getRemoteAddr();

		log.trace("[{}] 채팅방 대기실 접속.", ip);

		ModelAndView mav = new ModelAndView();
		
		Member member = null;
		
		try {
			
			// 접속한 멤버 정보
			member = memberService.findByIp(ip);
			mav.addObject("member", member);
			
			// 전체 멤버 목록
			mav.addObject("memberList", memberService.findAll());
			
			// 내가 입장 가능한 채팅방 목록
			mav.addObject("myChatRoomList", chatRoomService.findAllByMember(member));

			mav.addObject("AccessTypeANY", AccessType.ANY);
			mav.addObject("AccessTypePRO", AccessType.PROTECTED);

			mav.setViewName("chat/wait");
			
		} catch(ChatException e) {
			log.error("[{}][{}] {}", ip, e.getResponseCode(), e.getMessage());
			mav.addObject("message", e.getMessage());
			mav.setViewName("error");
		} catch(Exception e) {
			log.error("[{}][{}] {}", ip, ResponseCode.R9999, e.getMessage());
			mav.addObject("message", e.getMessage());
			mav.setViewName("error");
		}
		
		return mav;
	}

	/**
	 * [채팅방 생성 처리]
	 * Ajax 채팅방 생성 처리
	 */	
	@PostMapping(value="/chat/room")
	@ResponseBody
	public String createChatRoom(HttpServletRequest request, @RequestBody Map<String, Object> bodyMap) {
		
		String ip = request.getRemoteAddr();
		
		log.trace("[{}] 채팅방 생성 접근. {}", ip, bodyMap);
		
		ObjectMapper mapper = new ObjectMapper();
		ResponseCode code	= ResponseCode.R0000;
		String message 		= code.getMessage();

		Map<String, Object> resultHm = new HashMap<String, Object>();
		
		try {
			
			if(bodyMap == null || bodyMap.isEmpty()) {
				throw new ChatException(ResponseCode.R9998);
			}

			// 회원조회
			Member member = memberService.findByIp(ip);
			
			// 채팅방 생성
			AccessType accessType = AccessType.findByCodeStr((String) bodyMap.get("accessType"));
			List<Member> accessMemberList = memberService.findByAccessTypeAndMemberIpArray(accessType, (List<String>) bodyMap.get("accessMemberList"));

			// 채팅방 생성
			ChatRoom chatRoom = chatRoomService.generateChatRoomByBodyMap(member, bodyMap, accessMemberList);
			
			// 채팅방 저장
			chatRoomService.save(chatRoom);

			// 채팅방 가공
			chatRoom = chatRoomService.processChatRoom(chatRoom);

			log.trace("[{}] 채팅방 생성 객체 출력. {}", member.getName(), chatRoom);

			resultHm.put("data", chatRoom);
			
		} catch(ChatException e) {
			e.printStackTrace();
			code = e.getResponseCode();
			message = e.getMessage();
		} catch(Exception e) {
			e.printStackTrace();
			code = ResponseCode.R9999;
			message = code.getMessage();
		}
		
		try {
			resultHm.put("res_cd", code);
			resultHm.put("res_msg", message);
			return mapper.writeValueAsString(resultHm);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * [채팅방 수정 처리]
	 * Ajax 채팅방 수정 처리
	 */
	@PutMapping(value="/chat/room")
	@ResponseBody
	public String updateChatRoom(HttpServletRequest request, @RequestBody Map<String, Object> bodyMap) {

		String ip = request.getRemoteAddr();

		log.trace("[{}] 채팅방 수정 접근. {}", ip, bodyMap);

		ObjectMapper mapper = new ObjectMapper();
		ResponseCode code	= ResponseCode.R0000;
		String message 		= code.getMessage();

		Map<String, Object> resultHm = new HashMap<String, Object>();

		try {

			if(bodyMap == null || bodyMap.isEmpty()) {
				throw new ChatException(ResponseCode.R9998);
			}

			// 회원조회
			Member member = memberService.findByIp(ip);

			String id = (String) bodyMap.get("id");

			// 채팅방 조회
			ChatRoom chatRoom = chatRoomService.findById(id);

			// 기본 생성 채팅방인지 확인
			if ("SYSTEM".equals(chatRoom.getCreator().getName())) {
				log.trace("[{}] System ChatRoom 수정 접근... Error!", ip);
				throw new ChatException(ResponseCode.C9996);
			}

			// 채팅방 수정 권한 확인
			if (!chatRoom.getCreator().getIp().equals(member.getIp())) {
				throw new ChatException(ResponseCode.C9996);
			}

			// 채팅방 수정객체 변환
			AccessType accessType = AccessType.findByCodeStr((String) bodyMap.get("accessType"));
			List<Member> accessMemberList = memberService.findByAccessTypeAndMemberIpArray(accessType, (List<String>) bodyMap.get("accessMemberList"));
			chatRoomService.convertChatRoomByBodyMap(chatRoom, bodyMap, accessMemberList);

			// 채팅방 수정
			chatRoom = chatRoomService.update(chatRoom);
			
			// 채팅방 가공
			chatRoom = chatRoomService.processChatRoom(chatRoom);

			// 채팅방 상태 전파
			chatService.convertAndSendForRoomStatus(chatRoomService.processChatRoomForStatus(chatRoom));

			log.trace("[{}] 채팅방 수정 객체 출력. {}", member.getName(), chatRoom);

			resultHm.put("data", chatRoom);

		} catch(ChatException e) {
			e.printStackTrace();
			code = e.getResponseCode();
			message = e.getMessage();
		} catch(Exception e) {
			e.printStackTrace();
			code = ResponseCode.R9999;
			message = code.getMessage();
		}

		try {
			resultHm.put("res_cd", code);
			resultHm.put("res_msg", message);
			return mapper.writeValueAsString(resultHm);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * [채팅방 입장]
	 * 채팅방 입장 권한 확인
	 */
	@GetMapping(value="/chat/room/{id}")
	public ModelAndView joinChatRoom(HttpServletRequest request, @PathVariable(value = "id") String id) {
		
		String ip = request.getRemoteAddr();

		log.trace("[{}] 채팅방 [{}] 입장.", ip, id);
		
		ModelAndView mav = new ModelAndView();
		
		try {
			
			Member member = memberService.findByIp(ip);
			
			// 채팅방 조회
			ChatRoom chatRoom = chatRoomService.findById(id);

			// chatRoom 가공
			chatRoom = chatRoomService.processChatRoom(chatRoom);

			// 채팅방 활성화 여부 판단
			if (chatRoom.getStatus() == UseStatus.N) {
				throw new ChatException(ResponseCode.C9991);
			}
			
			// 채팅방 입장 가능 여부 판단
			boolean isPossibleJoin = chatRoomService.isPossibleJoin(chatRoom, member);
			
			if(!isPossibleJoin) {
				throw new ChatException(ResponseCode.C9994);
			}

			// 입장 가능 인원 조회
			List<Member> accessMemberList = chatRoomService.findAccessMemberListById(id);
			
			mav.addObject("member", member);
			mav.addObject("chatRoom", chatRoom);
			mav.addObject("accessMemberList", accessMemberList);
			mav.addObject("memberList", memberService.findAll());

			mav.setViewName("chat/room");
			
		} catch(ChatException e) {
			log.error("[{}][{}] {}", ip, e.getResponseCode(), e.getMessage());
			mav.addObject("message", e.getMessage());
			mav.setViewName("error");
		} catch(Exception e) {
			log.error("[{}][{}] {}", ip, ResponseCode.R9999, e.getMessage());
			mav.addObject("message", e.getMessage());
			mav.setViewName("error");
		}
		
		return mav;
	}

	/**
	 * [채팅방 파일 업로드]
	 * Ajax 채팅방 파일 업로드
	 */
	@PostMapping(value = "/chat/room/{id}/upload")
	@ResponseBody
	public String uploadFileToChatRoom(HttpServletRequest request, @PathVariable(value = "id") String id, @RequestPart("files") List<MultipartFile> uploadFiles) {

		String ip = request.getRemoteAddr();

		log.trace("[{}] 채팅방 [{}] 파일업로드.", ip, id);

		ObjectMapper mapper = new ObjectMapper();
		ResponseCode code	= ResponseCode.R0000;
		String messageStr	= code.getMessage();

		Map<String, Object> resultHm = new HashMap<String, Object>();

		try {

			Member member = memberService.findByIp(ip);

			// 채팅방 조회
			ChatRoom chatRoom = chatRoomService.findById(id);

			// 채팅방 입장 가능 여부 판단
			boolean isPossibleJoin = chatRoomService.isPossibleJoin(chatRoom, member);

			if(!isPossibleJoin) {
				throw new ChatException(ResponseCode.C9994);
			}

			log.trace("uploadFiles={}", uploadFiles);

			// 파일 업로드 처리 및 업로드 파일 전파
			for (MultipartFile file : uploadFiles) {
				FileDTO fileDTO = null;

				try {
					// 파일 저장
					fileDTO = fileService.save(chatRoom, file);

					// 파일정보 출력
					log.trace("[{}][{}] fileDTO={}", member.getIp(), chatRoom.getId(), fileDTO);

					// 채널타입 결정
					ChannelType usingType = ChannelType.FILE;

					if (serviceImages.contains(fileDTO.getExtend())) {
						usingType = ChannelType.IMAGE;
					}

					// Message 생성
					Message message = new Message();
					message.setSender(member);
					message.setChatRoomId(chatRoom.getId());
					message.setLink("/chat/download/" + fileDTO.getId());
					message.setMessage(fileDTO.getFileName());
					message.setType(usingType);

					chatRoom.getMessageList().add(message);

					// 메시지 전송
					chatService.convertAndSendForMessage(chatRoom, message);

				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

		} catch(ChatException e) {
			e.printStackTrace();
			code = e.getResponseCode();
			messageStr = e.getMessage();
		} catch(Exception e) {
			e.printStackTrace();
			code = ResponseCode.R9999;
			messageStr = code.getMessage();
		}

		try {
			resultHm.put("res_cd", code);
			resultHm.put("res_msg", messageStr);
			return mapper.writeValueAsString(resultHm);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * [채팅방 파일 다운로드]
	 */
	@GetMapping(value = "/chat/download/{id}")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "id") String id) {
		String ip = request.getRemoteAddr();

		log.trace("[{}][{}] 파일 다운로드.", ip, id);

		String errorMessage = "";

		try {

			Member member = memberService.findByIp(ip);

			// 저장된 파일 조회
			FileDTO fileDTO = fileService.findById(id);

			if (fileDTO == null) {
				throw new ChatException(ResponseCode.F9992);
			}

			String chatRoomId = fileDTO.getChatRoomId();

			log.trace("다운로드 채팅방 아이디={}", chatRoomId);

			// 채팅방 조회
			ChatRoom chatRoom = chatRoomService.findById(chatRoomId);

			// 채팅방 입장 가능 여부 판단
			boolean isPossibleJoin = chatRoomService.isPossibleJoin(chatRoom, member);

			if(!isPossibleJoin) {
				throw new ChatException(ResponseCode.C9994);
			}

			// 파일 스트리밍
			fileService.streamingFile(response, fileDTO);

		} catch (ChatException e) {
			e.printStackTrace();
			errorMessage = String.format("[%s] %s", e.getResponseCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = String.format("[%s] %s", ResponseCode.R9999, e.getMessage());
		} finally {
			if (!errorMessage.equals("")) {
				try {
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write(String.format("<script>alert('%s');</script>", errorMessage));
					response.getWriter().flush();
					response.getWriter().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * [채팅 메시지 수신]
	 */
	@MessageMapping(value = "/sendMessage")
	public void receiveMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor messageHeaderAccessor) {

		String ip = (String) Objects.requireNonNull(messageHeaderAccessor.getSessionAttributes()).get("ip");
		String sessionId = (String) Objects.requireNonNull(messageHeaderAccessor.getSessionAttributes()).get("sessionId");

		log.trace("[{}][{}] 채팅방 메시지 전송. {}", ip, sessionId, chatMessage);

		try {

			ip = memberService.changeHostIp(ip);

			// session에 저장된 ip와 client의 ip가 다를경우 오류처리
			String senderIp = chatMessage.getSenderIp();
			if(!ip.equals(senderIp)) {
				log.error("ip 정보 상이. senderIp={}, ip={}", senderIp, ip);
				throw new ChatException(ResponseCode.R9997);
			}

			Member member        = memberService.findByIp(ip);

			// 채팅방 조회
			ChatRoom chatRoom    = chatRoomService.findById(chatMessage.getChatRoomId());

			// 채팅방 입장 가능 여부 판단
			boolean isPossibleJoin = chatRoomService.isPossibleJoin(chatRoom, member);

			if(!isPossibleJoin) {
				throw new ChatException(ResponseCode.C9994);
			}

			// Message 변환 처리
			Message message = chatService.chatMessageToMessage(chatMessage, member);

			// Message sessionId 저장
			message.setSessionId(sessionId);

			// 메시지 전파
			chatService.receiveMessage(chatRoom, message);

		} catch (ChatException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}