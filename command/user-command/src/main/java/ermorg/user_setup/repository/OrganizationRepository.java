package ermorg.user_setup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	Organization save(Organization organization);

	// Fetch only non-deleted organization
	Optional<Organization> findByIdAndDeletedFalse(Long id);

	// Optional: get all active organizations
	List<Organization> findAllByDeletedFalse();
}
