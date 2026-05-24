package ermorg.erm.util.mapper;

import java.lang.reflect.Field;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

	// Reflection caching layers to eliminate runtime class traversal overhead
	private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Field[]> ALL_FIELDS_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, Field> RESOLVED_FIELD_NAME_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, Boolean> PATH_RESOLVABILITY_CACHE = new ConcurrentHashMap<>();

	public static CustomResponse map(Object response, CustomFieldResponse customField, String tableName)
			throws IllegalArgumentException, IllegalAccessException {

		if (response == null || customField == null) {
			return null;
		}

		String riskTitle = resolveRiskTitle(response, customField);
		if (riskTitle != null) {
			return buildResponse(customField.getFieldName(), riskTitle, customField);
		}

		String subRiskName = resolveSubRiskName(response, customField);
		if (subRiskName != null) {
			return buildResponse(customField.getFieldName(), subRiskName, customField);
		}

		String lookupDisplay = resolveLookupDisplayValue(response, customField);
		if (lookupDisplay != null) {
			return buildResponse(customField.getFieldName(), lookupDisplay, customField);
		}

		String systemField = customField.getSystemFieldName();
		boolean resolvable = isPathResolvable(response.getClass(), systemField, tableName);

		if (!resolvable) {
			if ("id".equals(systemField)) {
				return buildResponse("id", null, customField);
			}
			return null;
		}

		Object value = getNestedValue(response, systemField, tableName);

		CustomResponse customResponse = new CustomResponse();
		customResponse.setFieldName("id".equals(systemField) ? "id" : customField.getFieldName());
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

	private static String resolveRiskTitle(Object response, CustomFieldResponse customField) {
		if (response == null || !isRiskTitleField(customField)) {
			return null;
		}

		String directRiskTitle = getFieldValueAsString(response, "riskTitle");
		if (directRiskTitle != null) {
			return directRiskTitle;
		}

		Object risk = getFieldValue(response, "risk");
		if (risk == null) {
			return null;
		}

		String title = getFieldValueAsString(risk, "risktitle");
		return title != null ? title : getFieldValueAsString(risk, "riskTitle");
	}

	private static boolean isRiskTitleField(CustomFieldResponse customField) {
		if (customField == null) {
			return false;
		}

		return getFieldKeys(customField).stream()
				.map(CustomResponseMapperUtil::normalizeMatchKey)
				.anyMatch("risktitle"::equals);
	}

	private static String resolveSubRiskName(Object response, CustomFieldResponse customField) {
		if (response == null || !isSubRiskDisplayField(customField)) {
			return null;
		}

		String directSubRiskName = getFieldValueAsString(response, "subRiskName");
		if (directSubRiskName != null) {
			return directSubRiskName;
		}

		String subRiskCollectionNames = resolveSubRiskCollectionNames(response, customField);
		if (subRiskCollectionNames != null) {
			return subRiskCollectionNames;
		}

		Object subRisk = getFieldValue(response, "subRisk");
		if (subRisk == null) {
			return null;
		}

		String title = getFieldValueAsString(subRisk, "subRisk");
		return title != null ? title : getFieldValueAsString(subRisk, "subRiskName");
	}

	private static boolean isSubRiskDisplayField(CustomFieldResponse customField) {
		if (customField == null) {
			return false;
		}

		return getFieldKeys(customField).stream()
				.map(CustomResponseMapperUtil::normalizeMatchKey)
				.anyMatch(key -> "subriskid".equals(key)
						|| "subrisk".equals(key)
						|| "subriskname".equals(key)
						|| "risksubtitle".equals(key)
						|| "subrisktitle".equals(key));
	}

	private static String resolveSubRiskCollectionNames(Object response, CustomFieldResponse customField) {
		for (String fieldName : List.of("subRiskIds", "riskSubIds", "riskSubs", "subRisk")) {
			Object value = getFieldValue(response, fieldName);
			if (!(value instanceof Collection<?> collection)) {
				continue;
			}

			String names = collection.stream()
					.map(item -> extractSingleValue(item, customField))
					.filter(Objects::nonNull)
					.collect(Collectors.joining(", "));

			if (!names.isBlank()) {
				return names;
			}
		}

		return null;
	}

	private static String resolveLookupDisplayValue(Object response, CustomFieldResponse customField) {
		if (response == null || customField == null) {
			return null;
		}

		String key = getFieldKeys(customField).stream()
				.map(CustomResponseMapperUtil::normalizeMatchKey)
				.collect(Collectors.joining("|"));

		if (matchesAny(key, "function", "businessfunction")) {
			return getFieldValueAsString(response, "functionName");
		}

		if (matchesAny(key, "branch", "branchid")) {
			return getFieldValueAsString(response, "branchName");
		}

		if (matchesAny(key, "riskowner", "riskownerid", "owner")) {
			return getFieldValueAsString(response, "riskOwnerName");
		}

		if (matchesAny(key, "riskchampion", "riskchampionid", "champion")) {
			return getFieldValueAsString(response, "riskChampionName");
		}

		if (matchesAny(key, "businesssegment", "segment")) {
			return getFieldValueAsString(response, "businessSegmentName");
		}

		if (matchesAny(key, "businessvertical", "vertical")) {
			return getFieldValueAsString(response, "businessVerticalName");
		}

		return null;
	}

	private static boolean matchesAny(String combinedKey, String... expectedKeys) {
		if (combinedKey == null || combinedKey.isBlank()) {
			return false;
		}

		return Arrays.stream(combinedKey.split("\\|"))
				.anyMatch(key -> Arrays.stream(expectedKeys)
						.anyMatch(expected -> key.equals(expected) || key.contains(expected)));
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
		Object val = getFieldValue(obj, fieldName);
		return val != null ? val.toString() : null;
	}

	private static Object getFieldValue(Object obj, String fieldName) {
		Field field = getField(obj.getClass(), fieldName);
		if (field == null) {
			return null;
		}

		try {
			field.setAccessible(true);
			return field.get(obj);
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

	private static Field getField(Class<?> clazz, String name) {
		if (clazz == null || name == null) {
			return null;
		}
		String key = clazz.getName() + "#" + name;
		return FIELD_CACHE.computeIfAbsent(key, k -> {
			Class<?> current = clazz;
			while (current != null) {
				try {
					return current.getDeclaredField(name);
				} catch (NoSuchFieldException ignored) {
					current = current.getSuperclass();
				}
			}
			return null;
		});
	}

	private static Field[] getAllFields(Class<?> clazz) {
		if (clazz == null) {
			return new Field[0];
		}
		return ALL_FIELDS_CACHE.computeIfAbsent(clazz, c -> {
			List<Field> fields = new ArrayList<>();
			Class<?> current = c;
			while (current != null && current != Object.class) {
				fields.addAll(Arrays.asList(current.getDeclaredFields()));
				current = current.getSuperclass();
			}
			return fields.toArray(new Field[0]);
		});
	}

	private static Field findFieldByName(Class<?> clazz, String name) {
		if (clazz == null || name == null || name.isBlank()) {
			return null;
		}
		String key = clazz.getName() + "#" + name;
		return RESOLVED_FIELD_NAME_CACHE.computeIfAbsent(key, k -> {
			Field field = getField(clazz, name);
			if (field != null) {
				return field;
			}

			String normalizedTarget = normalizeMatchKey(name);
			for (Field f : getAllFields(clazz)) {
				if (normalizeMatchKey(f.getName()).equals(normalizedTarget)) {
					return f;
				}
			}
			return null;
		});
	}

	private static Object getNestedValue(Object obj, String path, String tableName) {
		if (obj == null || path == null || path.isBlank()) {
			return null;
		}

		if (obj instanceof Collection<?> collection) {
			List<Object> results = new ArrayList<>();
			for (Object item : collection) {
				Object res = getNestedValue(item, path, tableName);
				if (res != null) {
					if (res instanceof Collection<?> subColl) {
						results.addAll(subColl);
					} else {
						results.add(res);
					}
				}
			}
			return results.isEmpty() ? null : results;
		}

		if (path.contains(".")) {
			int dotIndex = path.indexOf('.');
			String segment = path.substring(0, dotIndex);
			String remaining = path.substring(dotIndex + 1);

			Field field = findFieldByName(obj.getClass(), segment);
			if (field != null) {
				try {
					field.setAccessible(true);
					Object val = field.get(obj);
					return getNestedValue(val, remaining, tableName);
				} catch (IllegalAccessException ignored) {
				}
			}

			String segmentNorm = normalizeMatchKey(segment);
			String classNorm = normalizeMatchKey(obj.getClass().getSimpleName());
			String tableNorm = normalizeMatchKey(tableName);

			if (segmentNorm.equals(classNorm) || segmentNorm.equals(tableNorm)) {
				return getNestedValue(obj, remaining, tableName);
			}

			return null;
		}

		if ("id".equalsIgnoreCase(path)) {
			Field idField = findFieldByName(obj.getClass(), "id");
			if (idField == null && tableName != null) {
				String expectedTableId = tableName.substring(0, 1).toLowerCase() + tableName.substring(1) + "Id";
				idField = findFieldByName(obj.getClass(), expectedTableId);
			}
			if (idField == null) {
				String expectedClassId = obj.getClass().getSimpleName().substring(0, 1).toLowerCase() + obj.getClass().getSimpleName().substring(1) + "Id";
				idField = findFieldByName(obj.getClass(), expectedClassId);
			}

			if (idField != null) {
				try {
					idField.setAccessible(true);
					return idField.get(obj);
				} catch (IllegalAccessException ignored) {
				}
			}
		}

		Field field = findFieldByName(obj.getClass(), path);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(obj);
			} catch (IllegalAccessException ignored) {
			}
		}

		return null;
	}

	private static boolean isPathResolvable(Class<?> clazz, String path, String tableName) {
		if (clazz == null || path == null || path.isBlank()) {
			return false;
		}
		String key = clazz.getName() + "#" + path + "#" + (tableName != null ? tableName : "");
		return PATH_RESOLVABILITY_CACHE.computeIfAbsent(key, k -> checkPathResolvability(clazz, path, tableName));
	}

	private static boolean checkPathResolvability(Class<?> clazz, String path, String tableName) {
		if (path.contains(".")) {
			int dotIndex = path.indexOf('.');
			String segment = path.substring(0, dotIndex);
			String remaining = path.substring(dotIndex + 1);

			Field field = findFieldByName(clazz, segment);
			if (field != null) {
				return isPathResolvable(field.getType(), remaining, tableName);
			}

			String segmentNorm = normalizeMatchKey(segment);
			String classNorm = normalizeMatchKey(clazz.getSimpleName());
			String tableNorm = normalizeMatchKey(tableName);

			if (segmentNorm.equals(classNorm) || segmentNorm.equals(tableNorm)) {
				return isPathResolvable(clazz, remaining, tableName);
			}

			return false;
		}

		if ("id".equalsIgnoreCase(path)) {
			return true;
		}

		return findFieldByName(clazz, path) != null;
	}
}
