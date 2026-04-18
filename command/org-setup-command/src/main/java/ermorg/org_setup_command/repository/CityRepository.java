package ermorg.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.org_setup_command.modal.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    City save(City city);
}
