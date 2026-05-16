package ermorg.erm.util.mapper;

import ermorg.erm.dto.riskDTO.LicenseResponseDTO;
import ermorg.erm.model.License;

public class LicenseMapper {

    public static LicenseResponseDTO toDTO(License license) {
        LicenseResponseDTO dto = new LicenseResponseDTO();

        dto.setId(license.getId());
        dto.setOrganizationId(
            license.getOrganization() != null ? license.getOrganization().getId() : null
        );

        dto.setPlanType(license.getPlanType());
        dto.setStartDate(license.getStartDate());
        dto.setEndDate(license.getEndDate());

        dto.setGracePeriodDays(license.getGracePeriodDays());
        dto.setStatus(
            license.getStatus() != null ? license.getStatus().name() : null
        );

        dto.setAutoRenew(license.getAutoRenew());
        dto.setLastRenewedAt(license.getLastRenewedAt());

        dto.setCreatedAt(
            license.getCreatedDate() != null ? license.getCreatedDate().atStartOfDay() : null
        );
        dto.setUpdatedAt(
            license.getUpdateDate() != null ? license.getUpdateDate().atStartOfDay() : null
        );

        return dto;
    }
}