package com.erm.erm_api_gateway.model;

import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Role extends BaseModel {

	private String name;
	private String description;
	private Long priority;

//    @ManyToOne//(cascade = CascadeType.ALL)
//    private Organization organization;

//    @ManyToOne
//    private Company company;

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
	private Set<RoleRight> roleRights;

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
		Role other = (Role) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name);
	}

}
