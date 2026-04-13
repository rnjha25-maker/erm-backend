package com.erm.model.history;

import com.erm.model.BaseModel;
import com.erm.model.User;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserHistory extends BaseModel{

	private String firstname;
	private String lastname;
	private String email;
	private String mobileNo;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
}
