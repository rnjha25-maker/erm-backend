package ermorg.erm.mapping;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AutoStrategyBuilder {

    private AutoStrategyBuilder() {
    }

    public static <T> Map<String, Function<T, Object>> build(
            Class<T> clazz,
            Supplier<Map<String, Function<T, Object>>> customStrategiesSupplier) {

        Map<String, Function<T, Object>> strategies = new HashMap<>();

        for (Method method : clazz.getMethods()) {
            if (isGetter(method)) {
                MethodHandle getter = toHandle(method);
                if (getter != null) {
                    strategies.put(n(extractFieldName(method.getName())), source -> invoke(getter, source));
                }
            }
        }

        if (customStrategiesSupplier != null) {
            Map<String, Function<T, Object>> customStrategies = customStrategiesSupplier.get();
            if (customStrategies != null) {
                strategies.putAll(customStrategies);
            }
        }

        return Map.copyOf(strategies);
    }

    public static <T> Map<String, Function<T, Object>> build(Class<T> clazz) {
        return build(clazz, null);
    }

    private static MethodHandle toHandle(Method method) {
        try {
            return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static Object invoke(MethodHandle getter, Object source) {
        try {
            return getter.invoke(source);
        } catch (Throwable e) {
            return null;
        }
    }

    private static boolean isGetter(Method method) {
        return method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class)
                && (method.getName().startsWith("get") || method.getName().startsWith("is"))
                && !method.getName().equals("getClass");
    }

    private static String extractFieldName(String methodName) {
        String name = methodName.startsWith("get")
                ? methodName.substring(3)
                : methodName.substring(2);

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static String n(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
