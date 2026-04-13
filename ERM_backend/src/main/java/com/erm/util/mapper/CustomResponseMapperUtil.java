package com.erm.util.mapper;

import java.lang.reflect.Field;

import com.erm.dto.response.CustomFieldResponse;
import com.erm.dto.response.CustomResponse;

public class CustomResponseMapperUtil {

	
	public static CustomResponse map(Object response, CustomFieldResponse customField, String tableName) throws IllegalArgumentException, IllegalAccessException {

		Field[] fields = response.getClass().getDeclaredFields();
		Field[] superFields = response.getClass().getSuperclass().getDeclaredFields();
		
		Field matchedField = null;
		
		// First, check if systemFieldName is "id" and look for tableName + "Id" field
		if ("id".equals(customField.getSystemFieldName())) {
			String expectedIdFieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1) + "Id";
			for (Field field : fields) {
				if (field.getName().equals(expectedIdFieldName)) {
					matchedField = field;
					break;
				}
			}
		}
		
		// If not found, check declared fields for exact match
		if (matchedField == null) {
			for (Field field : fields) {
				String name = field.getName();
				if (customField.getSystemFieldName().equals(name)) {
					matchedField = field;
					break;
				}
			}
		}
		
		// If still not found, check superclass fields
		if (matchedField == null) {
			for (Field superField : superFields) {
				String name = superField.getName();
				if (customField.getSystemFieldName().equals(name)) {
					matchedField = superField;
					break;
				}
			}
		}
		
		// Special handling for "id" systemFieldName: always create a response
		if (matchedField == null && "id".equals(customField.getSystemFieldName())) {
			CustomResponse customResponse = new CustomResponse();
			customResponse.setFieldName("id");
			customResponse.setValue(null);
			customResponse.setFieldType(customField.getFieldType());
			return customResponse;
		}
		
		// If no field found and not "id" systemFieldName, return null
		if (matchedField == null) {
			return null;
		}
		
		CustomResponse customResponse = new CustomResponse();
		matchedField.setAccessible(true);
		
		// Set fieldName: if systemFieldName is "id", always use "id" as fieldName
		if ("id".equals(customField.getSystemFieldName())) {
			// Always use "id" as fieldName when systemFieldName is "id"
			customResponse.setFieldName("id");
		} else {
			customResponse.setFieldName(customField.getFieldName());
		}
		
		Object object = matchedField.get(response);
		customResponse.setValue(object != null ? object.toString() : null);
		customResponse.setFieldType(customField.getFieldType());
		
		return customResponse;
	}
}
