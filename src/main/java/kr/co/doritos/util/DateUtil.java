package kr.co.doritos.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static String getyyyyMMdd() {
		return get("yyyyMMdd");
	}
	
	public static String get(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}
}
