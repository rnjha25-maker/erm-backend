package ermorg.erm.erm_command_organization.dto.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CityRequest {

	private List<CityDto> cities = new ArrayList<>();
}
