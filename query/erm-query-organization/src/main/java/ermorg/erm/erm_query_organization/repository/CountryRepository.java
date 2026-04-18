package ermorg.erm.erm_query_organization.repository;

import ermorg.erm.erm_query_organization.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}
