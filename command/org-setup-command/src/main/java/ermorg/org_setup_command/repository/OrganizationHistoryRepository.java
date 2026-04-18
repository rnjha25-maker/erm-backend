package ermorg.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.org_setup_command.modal.history.OrganizationHistory;

@Repository
public interface OrganizationHistoryRepository extends JpaRepository<OrganizationHistory, Long>{

}
