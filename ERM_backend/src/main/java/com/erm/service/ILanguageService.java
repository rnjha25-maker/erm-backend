package com.erm.service;

import com.erm.dto.riskDTO.LanguageRequest;
import com.erm.model.translation.Language;

public interface ILanguageService {
	
	public Language saveLanguage(LanguageRequest request);

}
