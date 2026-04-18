package ermorg.erm.dto.response;

import ermorg.erm.model.City;

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
