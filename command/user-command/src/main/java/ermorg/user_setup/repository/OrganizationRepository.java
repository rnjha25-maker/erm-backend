package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization save(Organization organization);
}
