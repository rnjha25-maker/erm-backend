package ermorg.erm.dto.riskDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageRequest {
	private long languageId;
	private String languageCode;
	private String languageName;
	private String description;

}
