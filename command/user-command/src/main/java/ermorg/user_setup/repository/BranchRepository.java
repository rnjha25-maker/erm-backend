package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Branch save(Branch branch);
}
