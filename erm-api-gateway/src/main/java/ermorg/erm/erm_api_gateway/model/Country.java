package ermorg.erm.erm_api_gateway.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Country extends BaseModel {
	private String name;
	private String code;

	@OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
	private List<State> states;
}
