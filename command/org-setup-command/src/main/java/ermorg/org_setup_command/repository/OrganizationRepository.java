package ermorg.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.org_setup_command.modal.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization save(Organization organization);
}
