package ermorg.erm.dto.response;

import ermorg.erm.model.Country;

import lombok.Data;

@Data
public class CountryResponse {
    private Long id;
    private String countryName;
    private String countryCode;

    public CountryResponse(Country country) {
        this.id = country.getId();
        this.countryName = country.getName();
        this.countryCode = country.getCode();
    }
}
