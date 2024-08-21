package kr.co.doritos.common;

/**
 * 채팅방 제한타입 
 */
public enum AccessType {

	ANY("ANY", "누구나"),
	PROTECTED("PROTECTED", "제한된"),
	;

	private final String code;
	private final String message;

	AccessType(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String  getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	public static AccessType findByCodeStr(String code) {
		for (AccessType accessType : AccessType.values()) {
			if (accessType.code.equals(code)) {
				return accessType;
			}
		}
		return null;
	}
}