package ermorg.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.org_setup_command.modal.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Country save(Country country);
}
