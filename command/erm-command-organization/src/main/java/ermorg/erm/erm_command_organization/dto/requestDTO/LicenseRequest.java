package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LicenseRequest {
	private Long organizationId;
	private String planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer gracePeriodDays;
    private Boolean autoRenew;
}
