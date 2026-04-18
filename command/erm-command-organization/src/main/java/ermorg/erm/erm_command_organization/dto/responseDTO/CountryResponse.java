package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.Country;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
