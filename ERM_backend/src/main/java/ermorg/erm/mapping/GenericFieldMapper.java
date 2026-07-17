package ermorg.erm.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component
public class GenericFieldMapper {

    private final Map<ModuleType, Map<String, Function<Object, Object>>> strategyRegistry;

    public GenericFieldMapper(List<FieldStrategy> strategies) {

        Map<ModuleType, Map<String, Function<Object, Object>>> registry =
                new EnumMap<>(ModuleType.class);

        for (FieldStrategy strategy : strategies) {
            if (strategy == null || strategy.getModuleType() == null) {
                continue;
            }
            registry.put(strategy.getModuleType(), Map.copyOf(strategy.getStrategies()));
        }

        this.strategyRegistry = Collections.unmodifiableMap(registry);
    }

    public Map<String, Object> mapFields(Object source,
                                         List<CustomFieldConfig> configs,
                                         ModuleType moduleType) {

        if (source == null || configs == null) {
            return Collections.emptyMap();
        }

        Map<String, Function<Object, Object>> strategies = moduleType != null
                ? strategyRegistry.getOrDefault(moduleType, Collections.emptyMap())
                : Collections.emptyMap();

        Map<String, Object> mapped = new LinkedHashMap<>(configs.size() * 2);

        for (CustomFieldConfig config : configs) {
            if (config == null) continue;

            Function<Object, Object> fn = findStrategy(strategies, config);

            Object value = (fn != null) ? safeApply(fn, source) : null;
            if (value == null) {
                value = resolveReflectively(source, config);
            }

            for (String key : keysFor(config)) {
                mapped.put(key, value);
            }
            if (config.getFieldName() != null && !config.getFieldName().isBlank()) {
                mapped.put(config.getFieldName(), value);
            }
            if (config.getSystemFieldName() != null && !config.getSystemFieldName().isBlank()) {
                mapped.put(config.getSystemFieldName(), value);
            }
        }

        return mapped;
    }

    public boolean hasStrategy(ModuleType moduleType) {
        return moduleType != null && strategyRegistry.containsKey(moduleType);
    }

    private Function<Object, Object> findStrategy(Map<String, Function<Object, Object>> strategies,
                                                  CustomFieldConfig config) {
        if (strategies == null || strategies.isEmpty()) {
            return null;
        }

        for (String key : keysFor(config)) {
            Function<Object, Object> fn = strategies.get(key);
            if (fn != null) {
                return fn;
            }
        }

        return null;
    }

    private Set<String> keysFor(CustomFieldConfig config) {
        Set<String> keys = new LinkedHashSet<>();
        addKey(keys, config.normalizedKey());
        addKey(keys, CustomFieldConfig.normalizeKey(config.getSystemFieldName()));
        addKey(keys, CustomFieldConfig.normalizeKey(config.getFieldName()));
        return keys;
    }

    private void addKey(Set<String> keys, String key) {
        if (key != null && !key.isBlank()) {
            keys.add(key);
        }
    }

    private Object resolveReflectively(Object source, CustomFieldConfig config) {
        for (String candidate : new String[] {config.getSystemFieldName(), config.getFieldName()}) {
            Object value = resolvePath(source, candidate);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Object resolvePath(Object source, String path) {
        if (source == null || path == null || path.isBlank()) {
            return null;
        }

        Object current = source;
        for (String segment : path.split("\\.")) {
            current = resolveSingle(current, segment);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object resolveSingle(Object source, String name) {
        if (source == null || name == null || name.isBlank()) {
            return null;
        }

        Method getter = findGetter(source.getClass(), name);
        if (getter != null) {
            try {
                return getter.invoke(source);
            } catch (Exception ignored) {
                return null;
            }
        }

        Field field = findField(source.getClass(), name);
        if (field == null) {
            return null;
        }

        try {
            field.setAccessible(true);
            return field.get(source);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private Method findGetter(Class<?> type, String name) {
        String normalizedName = CustomFieldConfig.normalizeKey(name);

        for (Method method : type.getMethods()) {
            if (method.getParameterCount() != 0 || method.getReturnType().equals(void.class)) {
                continue;
            }

            String methodName = method.getName();
            if (methodName.equals("getClass")) {
                continue;
            }

            if (methodName.equalsIgnoreCase("get" + name) || methodName.equalsIgnoreCase("is" + name)) {
                return method;
            }

            String propertyName = extractPropertyName(methodName);
            if (normalizedName.equals(CustomFieldConfig.normalizeKey(propertyName))) {
                return method;
            }
        }

        return null;
    }

    private String extractPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return methodName;
    }

    private Field findField(Class<?> type, String name) {
        String normalizedName = CustomFieldConfig.normalizeKey(name);
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(name)
                        || normalizedName.equals(CustomFieldConfig.normalizeKey(field.getName()))) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private Object safeApply(Function<Object, Object> fn, Object source) {
        try {
            return fn.apply(source);
        } catch (Exception e) {
            return null;
        }
    }
}
