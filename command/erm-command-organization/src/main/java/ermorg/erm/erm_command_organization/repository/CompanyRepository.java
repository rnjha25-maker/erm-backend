package ermorg.erm.erm_command_organization.repository;

import java.util.Optional;

import ermorg.erm.erm_command_organization.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company save(Company company);

    Optional<Company> findByIdAndDeletedFalse(Long id);
}
