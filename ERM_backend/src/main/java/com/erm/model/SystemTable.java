package com.erm.model;

import java.util.List;
import java.util.Objects;

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
@Table(name="system_tables")
public class SystemTable extends BaseModel{
	
	private String tableName;
	
	@OneToMany(mappedBy="systemTable", cascade = CascadeType.ALL)
	private List<SystemField> fields;

	@ManyToOne
	@JoinColumn(name="module_id")
	private Modules module;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fields);
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
		SystemTable other = (SystemTable) obj;
		return Objects.equals(fields, other.fields);
	}
	
	

}
