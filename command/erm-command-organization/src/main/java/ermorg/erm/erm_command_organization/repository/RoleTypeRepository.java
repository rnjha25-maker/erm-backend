package ermorg.erm.erm_command_organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.RoleType;

@Repository
public interface RoleTypeRepository extends JpaRepository<RoleType, Integer> {
    
    Optional<RoleType> findByCode(String code);

    List<RoleType> findAll();
}
