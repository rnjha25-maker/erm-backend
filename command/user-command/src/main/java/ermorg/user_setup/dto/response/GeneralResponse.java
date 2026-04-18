package ermorg.user_setup.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralResponse<T> {
	
	private T data;
	private ResponseStatus status;
	private String message;

}
