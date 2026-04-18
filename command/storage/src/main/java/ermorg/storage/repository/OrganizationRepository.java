package ermorg.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.storage.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
