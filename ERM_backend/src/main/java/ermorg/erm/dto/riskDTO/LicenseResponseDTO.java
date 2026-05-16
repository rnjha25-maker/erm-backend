package ermorg.erm.dto.riskDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LicenseResponseDTO {

    private Long id;
    private Long organizationId;

    private String planType;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer gracePeriodDays;
    private String status;

    private Boolean autoRenew;
    private LocalDateTime lastRenewedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}