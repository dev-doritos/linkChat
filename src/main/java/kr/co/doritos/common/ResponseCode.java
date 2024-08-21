package kr.co.doritos.common;

/**
 * 응답코드
 */
public enum ResponseCode {

	/* Common-Error */
	R0000("R0000", "Success"),
	R9999("R9999", "System Error"),
	R9998("R9998", "필수 정보 누락"),
	R9997("R9997", "클라이언트 오류"),
	
	/* Member-Error */
	M9991("M9991", "회원을 찾을 수 없습니다."),
	M9992("M9992", "이미 존재하는 회원 입니다."),

	/* File-Error */
	F9991("F9991", "파일 저장 실패"),
	F9992("F9992", "파일을 찾을 수 없습니다"),
	F9993("F9993", "파일 불러오기 실패"),
	
	/* Chat-Error */
	C9991("C9991", "채팅방을 찾을 수 없습니다."),
	C9992("C9992", "이미 존재하는 채팅방 입니다."),
	C9993("C9993", "생성 가능 채팅방 초과"),
	C9994("C9994", "입장할 수 없는 채팅방 입니다."),
	C9995("C9995", "이미 해당 회원으로 구성된 채팅방이 있습니다."),
	C9996("C9996", "채팅방 수정 권한이 없습니다"),
	;
	
	String code;
	String message;
	
	ResponseCode(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
