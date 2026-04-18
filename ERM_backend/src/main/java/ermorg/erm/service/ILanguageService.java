package ermorg.erm.service;

import ermorg.erm.dto.riskDTO.LanguageRequest;
import ermorg.erm.model.translation.Language;

public interface ILanguageService {
	
	public Language saveLanguage(LanguageRequest request);

}
