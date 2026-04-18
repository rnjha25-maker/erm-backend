package ermorg.erm.erm_command_organization.repository;

import ermorg.erm.erm_command_organization.dto.responseDTO.CityResponse;
import ermorg.erm.erm_command_organization.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    City save(City city);
}
