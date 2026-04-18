package ermorg.erm.erm_command_organization.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="system_view")
public class SystemView extends BaseModel{
	
	private String viewName;
	@OneToMany(mappedBy="systemView")
	private List<SystemViewField> fields = new ArrayList<>();
}
