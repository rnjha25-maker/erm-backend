package ermorg.erm.dto.response;

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