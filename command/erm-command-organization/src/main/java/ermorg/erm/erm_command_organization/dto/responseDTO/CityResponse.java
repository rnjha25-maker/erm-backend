package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.City;
import lombok.Data;

@Data
public class CityResponse {
    private Long id;
    private String cityName;

    public CityResponse(City city) {
        this.id = city.getId();
        this.cityName = city.getName();
    }
}
