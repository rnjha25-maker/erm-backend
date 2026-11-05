package ermorg.erm.erm_command_organization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ermorg.erm.erm_command_organization.controller.LicenseController;
import ermorg.erm.erm_command_organization.enums.LicenseStatus;
import ermorg.erm.erm_command_organization.model.License;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.repository.LicenseRepository;
import ermorg.erm.erm_command_organization.repository.OrganizationRepository;
import ermorg.erm.erm_command_organization.serviceimpl.LicenseServiceImpl;

class LicenseCreationTest {
    private final LicenseRepository licenses = mock(LicenseRepository.class);
    private final OrganizationRepository organizations = mock(OrganizationRepository.class);
    private MockMvc mvc;
    private final Organization organization = new Organization();
    private static final String BODY = """
        {"organizationId":101137,"planType":"Advance","startDate":"2026-10-18",
         "endDate":"2027-10-18","gracePeriodDays":15,"autoRenew":true}
        """;

    @BeforeEach void setup() {
        // Real Spring constructor injection: catches a dependency omitted from the constructor.
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(LicenseRepository.class, () -> licenses);
            context.registerBean(OrganizationRepository.class, () -> organizations);
            context.register(LicenseServiceImpl.class, LicenseController.class);
            context.refresh();
            mvc = MockMvcBuilders.standaloneSetup(context.getBean(LicenseController.class)).build();
        }
        when(organizations.findById(101137L)).thenReturn(Optional.of(organization));
        when(licenses.findByOrganizationIdAndStatusIn(101137L, List.of(LicenseStatus.ACTIVE)))
            .thenReturn(Optional.empty());
        when(licenses.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test void createBindsJsonAndUsesInjectedRepository() throws Exception {
        mvc.perform(post("/license/create").contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isOk()).andExpect(jsonPath("status").value("SUCCESS"))
            .andExpect(jsonPath("data.planType").value("Advance"))
            .andExpect(jsonPath("data.autoRenew").value(true));
        verify(licenses).save(argThat(value -> value.getOrganization() == organization
            && value.getStartDate().equals(LocalDate.of(2026, 10, 18))
            && value.getEndDate().equals(LocalDate.of(2027, 10, 18))
            && value.getGracePeriodDays() == 15 && value.getStatus() == LicenseStatus.ACTIVE));
    }

    @Test void existingActiveLicenseStillExpiresOnReplacement() throws Exception {
        var old = new License(); old.setStatus(LicenseStatus.ACTIVE);
        when(licenses.findByOrganizationIdAndStatusIn(101137L, List.of(LicenseStatus.ACTIVE)))
            .thenReturn(Optional.of(old));
        mvc.perform(post("/license/create").contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isOk());
        assertThat(old.getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        verify(licenses).save(old);
        verify(licenses, times(2)).save(any());
    }

    @Test void updateAlsoBindsJsonAndKeepsOrganization() throws Exception {
        var existing = new License(); existing.setOrganization(organization);
        when(licenses.findById(7L)).thenReturn(Optional.of(existing));
        mvc.perform(put("/license/update/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isOk()).andExpect(jsonPath("data.planType").value("Advance"));
        assertThat(existing.getOrganization()).isSameAs(organization);
        assertThat(existing.getEndDate()).isEqualTo(LocalDate.of(2027, 10, 18));
    }

    @Test void malformedJsonDoesNotSave() throws Exception {
        mvc.perform(post("/license/create").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(licenses, organizations);
    }
}
