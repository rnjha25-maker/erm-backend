package ermorg.erm.erm_query_organization.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralResponse<T> {
	private T data;
	private String message;
	private ResponseStatus status;
}
