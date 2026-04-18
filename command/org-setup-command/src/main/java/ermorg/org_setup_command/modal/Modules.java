package ermorg.org_setup_command.modal;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Modules extends BaseModel{

	private String name;
	private String description;
	@OneToMany(mappedBy="module")
	private List<Right> rights;
	
	@OneToMany(mappedBy="module")
	private List<Category> categories = new ArrayList<>();
}
