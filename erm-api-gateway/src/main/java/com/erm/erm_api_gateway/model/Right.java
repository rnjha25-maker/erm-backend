package com.erm.erm_api_gateway.model;

import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rights")
public class Right extends BaseModel {
	private String name;
	private String description;

	@ManyToOne // (cascade=CascadeType.MERGE)
	@JoinColumn(name = "organization_id")
	private Organization organization;
	@OneToMany(mappedBy = "right", cascade = CascadeType.ALL)
	private Set<RoleRight> roleRights;

	@ManyToOne
	@JoinColumn(name = "module_id")
	private Modules module;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(description, name);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Right other = (Right) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name);
	}

}
