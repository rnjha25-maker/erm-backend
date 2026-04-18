package ermorg.erm.erm_command_organization.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="system_view_fields")
public class SystemViewField extends BaseModel{

	private String field;
	@ManyToOne
	@JoinColumn(name="system_view_id")
	private SystemView systemView;
}
