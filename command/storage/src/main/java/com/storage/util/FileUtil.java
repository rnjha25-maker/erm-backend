package com.storage.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class FileUtil {

	/** Decodes data URL ({@code data:...;base64,...}) or raw Base64 to bytes. */
	public static byte[] decodeBase64ToBytes(String fileContent) {
		if (fileContent != null && fileContent.contains(",")) {
			return Base64.getDecoder().decode(fileContent.split(",", 2)[1]);
		}
		return Base64.getDecoder().decode(fileContent);
	}

	public static InputStream toInputStream(String fileContent) {
		return new ByteArrayInputStream(decodeBase64ToBytes(fileContent));
	}
	
	public static String  toBase64String(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

}
