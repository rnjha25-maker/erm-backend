package ermorg.erm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import ermorg.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
 
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicenseRequest {

    @JsonProperty("organizationId")
    private Long organizationId;

    @JsonProperty("planType")
    private String planType;

    @JsonProperty("startDate")
    private LocalDate startDate;

    @JsonProperty("endDate")
    private LocalDate endDate;

    @JsonProperty("gracePeriodDays")
    private Integer gracePeriodDays;

    @JsonProperty("autoRenew")
    private Boolean autoRenew;
}