package ermorg.erm.util.mapper;

import java.lang.reflect.Field;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ermorg.erm.constant.BusinessVertical;
import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.constant.ErmStakeholderRole;
import ermorg.erm.constant.Functions;
import ermorg.erm.constant.Impact;
import ermorg.erm.constant.Likelihood;
import ermorg.erm.constant.Period;
import ermorg.erm.constant.Priority;
import ermorg.erm.constant.RiskCategory;
import ermorg.erm.constant.RiskSubCategory;
import ermorg.erm.constant.Velocity;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;

public class CustomResponseMapperUtil {

	private static final List<Class<? extends Enum<?>>> DROPDOWN_ENUMS = List.of(
			BusinessVertical.class,
			ErmDashboardPeriodType.class,
			ErmStakeholderRole.class,
			Functions.class,
			Impact.class,
			Likelihood.class,
			Period.class,
			Priority.class,
			RiskCategory.class,
			RiskSubCategory.class,
			Velocity.class
	);

	public static CustomResponse map(Object response, CustomFieldResponse customField, String tableName)
			throws IllegalArgumentException, IllegalAccessException {

		Field matchedField = findField(response, customField, tableName);

		if (matchedField == null && "id".equals(customField.getSystemFieldName())) {
			return buildResponse("id", null, customField);
		}

		if (matchedField == null) {
			return null;
		}

		matchedField.setAccessible(true);
		Object value = matchedField.get(response);

		CustomResponse customResponse = new CustomResponse();
		customResponse.setFieldName("id".equals(customField.getSystemFieldName()) ? "id" : customField.getFieldName());
		customResponse.setValue(extractValue(value, customField));
		customResponse.setFieldType(customField.getFieldType());

		return customResponse;
	}

	private static CustomResponse buildResponse(String fieldName, String value, CustomFieldResponse customField) {
		CustomResponse response = new CustomResponse();
		response.setFieldName(fieldName);
		response.setValue(value);
		response.setFieldType(customField.getFieldType());
		return response;
	}

	private static String extractValue(Object value, CustomFieldResponse customField) {
		if (value == null) {
			return null;
		}

		if (value instanceof Collection<?> collection) {
			return collection.stream()
					.map(item -> extractSingleValue(item, customField))
					.filter(Objects::nonNull)
					.collect(Collectors.joining(", "));
		}

		String dropdownValue = resolveDropdownValue(value, customField);
		if (dropdownValue != null) {
			return dropdownValue;
		}

		return extractSingleValue(value, customField);
	}

	private static String extractSingleValue(Object obj, CustomFieldResponse customField) {
		if (obj == null) {
			return null;
		}

		if (isSimpleValue(obj)) {
			return obj.toString();
		}

		for (String fieldName : getDisplayFieldCandidates(customField)) {
			String fieldValue = getFieldValueAsString(obj, fieldName);
			if (fieldValue != null) {
				return fieldValue;
			}
		}

		String textValue = getFirstNonIdTextFieldValue(obj);
		if (textValue != null) {
			return textValue;
		}

		Field idField = getField(obj.getClass(), "id");
		if (idField != null) {
			try {
				idField.setAccessible(true);
				Object val = idField.get(obj);
				if (val != null) {
					return val.toString();
				}
			} catch (IllegalAccessException ignored) {
			}
		}

		return obj.toString();
	}

	private static List<String> getDisplayFieldCandidates(CustomFieldResponse customField) {
		Set<String> candidates = new LinkedHashSet<>();

		for (String fieldKey : getFieldKeys(customField)) {
			candidates.add(fieldKey + "Name");
			candidates.add(fieldKey + "Title");
			candidates.add(fieldKey + "Value");
			candidates.add(fieldKey + "Label");
			candidates.add(fieldKey);
		}

		candidates.addAll(List.of("name", "title", "value", "label", "displayName", "description"));

		return new ArrayList<>(candidates);
	}

	private static List<String> getFieldKeys(CustomFieldResponse customField) {
		List<String> keys = new ArrayList<>();

		if (customField == null) {
			return keys;
		}

		addFieldKey(keys, customField.getSystemFieldName());
		addFieldKey(keys, customField.getFieldName());

		return keys;
	}

	private static void addFieldKey(List<String> keys, String value) {
		String key = normalizeFieldKey(value);
		if (key != null && !keys.contains(key)) {
			keys.add(key);
		}
	}

	private static String normalizeFieldKey(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		String[] parts = value.trim().split("[^A-Za-z0-9]+");
		StringBuilder key = new StringBuilder();

		for (String part : parts) {
			if (part.isBlank()) {
				continue;
			}

			if (key.isEmpty()) {
				key.append(part.substring(0, 1).toLowerCase()).append(part.substring(1));
			} else {
				key.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
			}
		}

		return key.isEmpty() ? null : key.toString();
	}

	private static String getFieldValueAsString(Object obj, String fieldName) {
		Field field = getField(obj.getClass(), fieldName);
		if (field == null) {
			return null;
		}

		try {
			field.setAccessible(true);
			Object val = field.get(obj);
			return val != null ? val.toString() : null;
		} catch (IllegalAccessException ignored) {
			return null;
		}
	}

	private static String getFirstNonIdTextFieldValue(Object obj) {
		return Arrays.stream(getAllFields(obj.getClass()))
				.filter(field -> !field.getName().toLowerCase().contains("id"))
				.filter(field -> CharSequence.class.isAssignableFrom(field.getType()))
				.map(field -> getFieldValueAsString(obj, field.getName()))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private static String resolveDropdownValue(Object value, CustomFieldResponse customField) {
		Integer ordinal = getOrdinal(value);

		if (ordinal == null || customField == null || customField.getFieldType() == null
				|| !customField.getFieldType().toLowerCase().contains("dropdown")) {
			return null;
		}

		Class<? extends Enum<?>> enumClass = getDropdownEnumClass(customField);
		if (enumClass == null) {
			return null;
		}

		Enum<?>[] constants = enumClass.getEnumConstants();

		if (ordinal < 0 || ordinal >= constants.length) {
			return null;
		}

		return constants[ordinal].name();
	}

	private static Class<? extends Enum<?>> getDropdownEnumClass(CustomFieldResponse customField) {
		String fieldKey = getFieldKeys(customField).stream()
				.map(CustomResponseMapperUtil::normalizeMatchKey)
				.collect(Collectors.joining("|"));

		return DROPDOWN_ENUMS.stream()
				.filter(enumClass -> isMatchingEnum(fieldKey, enumClass))
				.findFirst()
				.orElse(null);
	}

	private static Integer getOrdinal(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}

		if (value instanceof String text && text.matches("-?\\d+")) {
			return Integer.parseInt(text);
		}

		return null;
	}

	private static boolean isMatchingEnum(String fieldKey, Class<? extends Enum<?>> enumClass) {
		String enumKey = normalizeMatchKey(enumClass.getSimpleName());

		return Arrays.stream(fieldKey.split("\\|"))
				.anyMatch(key -> !key.isBlank() && (key.contains(enumKey) || enumKey.contains(key)));
	}

	private static String normalizeMatchKey(String value) {
		if (value == null) {
			return "";
		}

		String key = value.toLowerCase().replaceAll("[^a-z0-9]", "");

		if (key.endsWith("ies")) {
			return key.substring(0, key.length() - 3) + "y";
		}

		if (key.endsWith("s")) {
			return key.substring(0, key.length() - 1);
		}

		return key;
	}

	private static boolean isSimpleValue(Object value) {
		return value instanceof String
				|| value instanceof Number
				|| value instanceof Boolean
				|| value instanceof Character
				|| value instanceof TemporalAccessor
				|| value.getClass().isEnum();
	}

	private static Field findField(Object response, CustomFieldResponse customField, String tableName) {
		String systemField = customField.getSystemFieldName();

		if ("id".equals(systemField)) {
			String expected = tableName.substring(0, 1).toLowerCase() + tableName.substring(1) + "Id";

			Field field = getField(response.getClass(), expected);
			if (field != null) {
				return field;
			}
		}

		return getField(response.getClass(), systemField);
	}

	private static Field getField(Class<?> clazz, String name) {
	    while (clazz != null) {
	        try {
				return clazz.getDeclaredField(name);
			} catch (NoSuchFieldException ignored) {
				clazz = clazz.getSuperclass();
			}
	    }
	    return null;
	}

	private static Field[] getAllFields(Class<?> clazz) {
		List<Field> fields = new java.util.ArrayList<>();
		while (clazz != null && clazz != Object.class) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields.toArray(new Field[0]);
	}
}
