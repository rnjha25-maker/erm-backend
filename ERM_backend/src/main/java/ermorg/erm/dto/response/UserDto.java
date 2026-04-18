package ermorg.erm.dto.response;

import ermorg.erm.model.User;

import lombok.Data;

@Data
public class UserDto {

	private long userId;
	private String name;
	private String email;
	
	public UserDto(User user) {
		this.userId = user.getId();
		this.name = user.getUserDetail().getFirstName() + " " + user.getUserDetail().getLastName();
		this.email = user.getEmail();
	}
	
}
