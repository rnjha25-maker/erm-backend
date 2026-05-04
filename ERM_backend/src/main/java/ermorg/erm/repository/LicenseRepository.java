package ermorg.erm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.License;
import ermorg.erm.util.mapper.LicenseStatus;



@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

	Optional<License> findByOrganizationIdAndStatusIn(Long organizationId, List<LicenseStatus> statuses);

	List<License> findByOrganizationIdOrderByEndDateDesc(Long orgId);
}