package kr.co.doritos.util;

import java.lang.reflect.Field;

public class ReflectUtil {

	/**
	 * @param Class<?> sourceClazz
	 * @param String fieldName 
	 */
	public static Object getDeclaredField( Class<?> sourceClazz, String fieldName ) throws RuntimeException, IllegalArgumentException, IllegalAccessException {
		
		Object fieldObject = null;
		boolean isFind = false; 
		
		Field [] fields = sourceClazz.getDeclaredFields();
		
		if(fields.length == 0) {
			throw new RuntimeException(String.format("%s 내 선언된 필드가 없습니다.", sourceClazz.getName()));
		}
		
		for(Field field : fields) {
			if(field.getName().equals(fieldName)) {
				field.setAccessible(true);
				
				fieldObject = field.get(fieldObject);

				isFind = true;
				
				break;
			}
		}
		
		if(!isFind) {
			throw new IllegalArgumentException(String.format("%s.%s 을(를) 찾을 수 없습니다.", sourceClazz.getName(), fieldName));
		}
			
		
		return fieldObject;
	}
}
