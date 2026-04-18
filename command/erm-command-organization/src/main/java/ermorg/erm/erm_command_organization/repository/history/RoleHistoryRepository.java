package ermorg.erm.erm_command_organization.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.history.RoleHistory;

@Repository
public interface RoleHistoryRepository extends JpaRepository<RoleHistory, Long> {

}
