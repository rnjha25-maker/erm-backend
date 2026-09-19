package ermorg.erm.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.*;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IUserService;

class DepartmentResponseTest {
    @Test void kriRetainsDepartmentIdAndResolvesNameIndependentlyOfStakeholder() {
        var repository = mock(DepartmentRepository.class);
        var department = new Department(); department.setName("Legal & Compliance"); department.setDeleted(false);
        when(repository.findById(14413L)).thenReturn(Optional.of(department));
        var utils = new FieldMapperUtils(mock(IUserService.class), repository);
        var service = new KripKpiRiskService();
        ReflectionTestUtils.setField(service, "fieldMapperUtils", utils);
        var entity = new KriKpiReview(); entity.setId(1L);
        entity.setBusinessFunction("14413"); entity.setStakeholderDepartments("Operations");
        KriKpiReviewResponseDTO dto = ReflectionTestUtils.invokeMethod(service, "toResponse", entity);
        assertThat(dto.getBusinessFunction()).isEqualTo("14413");
        assertThat(dto.getDepartmentName()).isEqualTo("Legal & Compliance");
        assertThat(dto.getStakeholderDepartments()).isEqualTo("Operations");
    }

    @Test void kpaReturnsResolvedDepartmentNamesAndUnknownIdsRemainVisible() {
        var repository = mock(DepartmentRepository.class);
        var department = new Department(); department.setName("Legal & Compliance"); department.setDeleted(false);
        when(repository.findById(14413L)).thenReturn(Optional.of(department));
        var utils = new FieldMapperUtils(mock(IUserService.class), repository);
        var service = new KpaKpiReviewService();
        ReflectionTestUtils.setField(service, "fieldMapperUtils", utils);
        var entity = new KpaKpiReview(); entity.setId(1L); entity.setBusinessFunction("14413");
        var dto = service.toResponse(entity);
        assertThat(dto.getDepartmentName()).isEqualTo("Legal & Compliance");
        assertThat(dto.getBusinessFunction()).isEqualTo("Legal & Compliance");
        assertThat(utils.resolveDepartmentFromObject("99999")).isEqualTo("99999");
        assertThat(utils.resolveDepartmentFromObject("Operations")).isEqualTo("Operations");
    }
}
