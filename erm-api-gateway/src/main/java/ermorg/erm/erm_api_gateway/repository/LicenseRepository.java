package ermorg.erm.erm_api_gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_api_gateway.enums.LicenseStatus;
import ermorg.erm.erm_api_gateway.model.License;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

	Optional<License> findByOrganizationIdAndStatusIn(Long organizationId, List<LicenseStatus> statuses);
}
