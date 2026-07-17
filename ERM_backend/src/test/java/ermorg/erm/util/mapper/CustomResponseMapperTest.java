package ermorg.erm.util.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.mapping.GenericFieldMapper;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.service.IFieldService;
import ermorg.erm.service.IUserService;

class CustomResponseMapperTest {

    @Test
    void shouldResolveValueBySystemFieldKeyWhenDisplayNameDiffers() throws Exception {
        IFieldService fieldService = mock(IFieldService.class);
        GenericFieldMapper genericFieldMapper = mock(GenericFieldMapper.class);
        IUserService userService = mock(IUserService.class);
        FieldMapperUtils fieldMapperUtils = new FieldMapperUtils(userService);

        CustomResponseMapper mapper = new CustomResponseMapper();
        ReflectionTestUtils.setField(mapper, "fieldService", fieldService);
        ReflectionTestUtils.setField(mapper, "genericFieldMapper", genericFieldMapper);
        ReflectionTestUtils.setField(mapper, "fieldMapperUtils", fieldMapperUtils);

        CustomFieldResponse customField = new CustomFieldResponse();
        customField.setFieldName("Display Label");
        customField.setFieldType("Input Field");
        customField.setSystemFieldName("riskTitle");

        when(fieldService.getCustomFieldResponse(1L, "risk")).thenReturn(List.of(customField));
        when(genericFieldMapper.hasStrategy(any())).thenReturn(true);
        when(genericFieldMapper.mapFields(any(), any(), any())).thenReturn(Map.of("riskTitle", "Quarterly review"));

        List<CustomResponse> responses = mapper.map("risk", 1L, new Object(), false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFieldName()).isEqualTo("Display Label");
        assertThat(responses.get(0).getValue()).isEqualTo("Quarterly review");
    }

    @Test
    void shouldResolveDatabaseStyleSystemFieldForTableWithoutRegisteredStrategy() throws Exception {
        IFieldService fieldService = mock(IFieldService.class);
        IUserService userService = mock(IUserService.class);
        FieldMapperUtils fieldMapperUtils = new FieldMapperUtils(userService);
        GenericFieldMapper genericFieldMapper = new GenericFieldMapper(List.of());

        CustomResponseMapper mapper = new CustomResponseMapper();
        ReflectionTestUtils.setField(mapper, "fieldService", fieldService);
        ReflectionTestUtils.setField(mapper, "genericFieldMapper", genericFieldMapper);
        ReflectionTestUtils.setField(mapper, "fieldMapperUtils", fieldMapperUtils);

        CustomFieldResponse customField = new CustomFieldResponse();
        customField.setFieldName("Key Performance Indicator");
        customField.setFieldType("Input Field");
        customField.setSystemFieldName("key_performance_indicator");
        customField.setShowGridColumn(true);

        when(fieldService.getCustomFieldResponse(1L, "kpaKpiReview")).thenReturn(List.of(customField));

        List<CustomResponse> responses = mapper.map("kpaKpiReview", 1L, new KpaResponse("Revenue Growth"), false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFieldName()).isEqualTo("Key Performance Indicator");
        assertThat(responses.get(0).getValue()).isEqualTo("Revenue Growth");
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
