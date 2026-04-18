package ermorg.erm.erm_api_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_api_gateway.model.Organization;
import ermorg.erm.erm_api_gateway.model.Right;

@Repository
public interface RightRepository extends JpaRepository<Right, Long> {

	public Right getRightByOrganization(Organization organization);
}
