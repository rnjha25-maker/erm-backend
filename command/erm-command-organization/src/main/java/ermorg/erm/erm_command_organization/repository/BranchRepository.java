package ermorg.erm.erm_command_organization.repository;

import java.util.Optional;

import ermorg.erm.erm_command_organization.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Branch save(Branch branch);

    Optional<Branch> findByIdAndDeletedFalse(Long id);
}
