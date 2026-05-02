package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicenseValidationResponse {

    private boolean valid;
    private String status;
    private LocalDate endDate;
    private Integer daysRemaining;
    private boolean inGracePeriod;
}