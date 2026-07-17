package ermorg.erm.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class GenericFieldMapperTest {

    @Test
    void mapFields_shouldResolveDatabaseStyleSystemFieldAgainstDtoGetterWithoutStrategy() {
        GenericFieldMapper mapper = new GenericFieldMapper(Collections.emptyList());

        CustomFieldConfig config = new CustomFieldConfig();
        config.setFieldName("Key Performance Indicator");
        config.setSystemFieldName("key_performance_indicator");

        Map<String, Object> values = mapper.mapFields(
                new KpaResponse("Revenue Growth"),
                List.of(config),
                null);

        assertThat(values)
                .containsEntry("keyperformanceindicator", "Revenue Growth")
                .containsEntry("Key Performance Indicator", "Revenue Growth")
                .containsEntry("key_performance_indicator", "Revenue Growth");
    }

    private static class KpaResponse {
        private final String keyPerformanceIndicator;

        private KpaResponse(String keyPerformanceIndicator) {
            this.keyPerformanceIndicator = keyPerformanceIndicator;
        }

        public String getKeyPerformanceIndicator() {
            return keyPerformanceIndicator;
        }
    }
}
