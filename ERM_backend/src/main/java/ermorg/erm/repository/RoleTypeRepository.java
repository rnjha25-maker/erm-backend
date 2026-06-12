package ermorg.erm.repository;

import ermorg.erm.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleTypeRepository extends JpaRepository<RoleType, Integer> {
    
    Optional<RoleType> findByCode(String code);
    
}
