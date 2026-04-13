package com.erm.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class FileUtil {
	
	public static InputStream toInputStream(String fileContent) {
		
		byte[] bytes;
		if(fileContent != null && fileContent.contains(",")) {
			
			bytes = Base64.getDecoder().decode(fileContent.split(",")[1]);
			
		}else {
			bytes = Base64.getDecoder().decode(fileContent);

		}
		
		return new ByteArrayInputStream(bytes);
	}
	
	public static String  toBase64String(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

}
