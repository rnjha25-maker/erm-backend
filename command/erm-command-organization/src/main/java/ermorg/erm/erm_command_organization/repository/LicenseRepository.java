package ermorg.erm.erm_command_organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.enums.LicenseStatus;
import ermorg.erm.erm_command_organization.model.License;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

	Optional<License> findByOrganizationIdAndStatusIn(Long organizationId, List<LicenseStatus> statuses);

	List<License> findByOrganizationIdOrderByEndDateDesc(Long orgId);

}