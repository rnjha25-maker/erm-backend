package ermorg.erm.mapping;

import java.util.Map;
import java.util.function.Function;

public interface FieldStrategy {

    ModuleType getModuleType();

    Map<String, Function<Object, Object>> getStrategies();
}
