package ermorg.erm.mapping;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        if (source == null || configs == null || moduleType == null) {
            return Collections.emptyMap();
        }

        Map<String, Function<Object, Object>> strategies = strategyRegistry.get(moduleType);
        if (strategies == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> mapped = new LinkedHashMap<>(configs.size());

        for (CustomFieldConfig config : configs) {
            if (config == null) continue;

            Function<Object, Object> fn = strategies.get(config.normalizedKey());

            Object value = (fn != null) ? safeApply(fn, source) : null;

            mapped.put(config.getFieldName(), value);
        }

        return mapped;
    }

    public boolean hasStrategy(ModuleType moduleType) {
        return moduleType != null && strategyRegistry.containsKey(moduleType);
    }

    private Object safeApply(Function<Object, Object> fn, Object source) {
        try {
            return fn.apply(source);
        } catch (Exception e) {
            return null;
        }
    }
}
