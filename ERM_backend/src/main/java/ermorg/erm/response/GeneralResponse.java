package ermorg.erm.response;

import ermorg.erm.dto.ResponseStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralResponse<T> {
	private T data;
	private String message;
	private ResponseStatus status;
	

}
