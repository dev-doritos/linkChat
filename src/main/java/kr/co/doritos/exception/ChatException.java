package kr.co.doritos.exception;

import kr.co.doritos.common.ResponseCode;
import lombok.Getter;

@Getter
public class ChatException extends RuntimeException {

	private ResponseCode responseCode;
	private String message;
	
	public ChatException(Throwable e) {
		super(e);
	}
	
	public ChatException(ResponseCode responseCode) {
		this.responseCode = responseCode;
		this.message = responseCode.getMessage();
	}
	
	public ChatException(ResponseCode responseCode, String message) {
		this.responseCode = responseCode;
		this.message = message;
	}
}
