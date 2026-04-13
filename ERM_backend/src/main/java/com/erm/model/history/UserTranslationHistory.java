package com.erm.model.history;

import com.erm.model.BaseModel;
import com.erm.model.User;
import com.erm.model.translation.Language;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserTranslationHistory extends BaseModel{
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="user_id")
	private User user;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="language_id")
	private Language language;
	
	private String operation;

}
