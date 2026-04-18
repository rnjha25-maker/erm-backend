package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company save(Company company);
}
