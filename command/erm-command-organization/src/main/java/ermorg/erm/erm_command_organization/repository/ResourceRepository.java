package ermorg.erm.erm_command_organization.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Optional<Resource> findByResourceCodeAndDeletedFalse(String resourceCode);

    List<Resource> findByResourceCodeInAndDeletedFalse(Collection<String> resourceCodes);

    List<Resource> findByDeletedFalseOrderByDisplayOrderAscNameAsc();

    boolean existsByResourceCodeIgnoreCase(String resourceCode);
}
