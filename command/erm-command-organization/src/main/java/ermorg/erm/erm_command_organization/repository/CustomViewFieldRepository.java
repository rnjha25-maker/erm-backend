package ermorg.erm.erm_command_organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.CustomViewFields;

@Repository
public interface CustomViewFieldRepository extends JpaRepository<CustomViewFields, Long> {

}
