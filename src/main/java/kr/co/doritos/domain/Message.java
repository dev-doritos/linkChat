package kr.co.doritos.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.doritos.common.ChannelType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Message {

	private ChannelType type;
	private Member sender;
	private String message;
	private String chatRoomId;
	private String link;
	private String auth_ymd;
	private String auth_hms;
	private String sessionId;

	public Message() {
		super();
		this.auth_ymd = new SimpleDateFormat("yyyyMMdd").format(new Date());
		this.auth_hms = new SimpleDateFormat("HHmmss").format(new Date()); 
	}
}