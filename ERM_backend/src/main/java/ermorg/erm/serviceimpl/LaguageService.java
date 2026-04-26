package ermorg.erm.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.riskDTO.LanguageRequest;
import ermorg.erm.model.history.LanguageHistory;
import ermorg.erm.model.translation.Language;
import ermorg.erm.repository.LanguageHistoryRepository;
import ermorg.erm.repository.LanguageRepository;
import ermorg.erm.service.ILanguageService;
import ermorg.erm.util.AuditorAwareImpl;

import jakarta.transaction.Transactional;

@Service
public class LaguageService implements ILanguageService {

	@Autowired
	private  LanguageRepository languageRepository;
	
	@Autowired
	private LanguageHistoryRepository languageHistoryRepository;
	
	@Autowired
	private AuditorAware auditAware;
	
	@Transactional
	@Override
	public Language saveLanguage(LanguageRequest request) {
		
//		String clientIp = ((AuditorAwareImpl) auditAware).getClientIp();

		Language language = languageRepository.findById(request.getLanguageId()).orElse(new Language());
		
		language.setDescription(request.getDescription());
		language.setLanguageCode(request.getLanguageCode());
		language.setLanguageName(request.getLanguageName());
//		language.setRequesterIP(clientIp);
		
		Language savedLanguage = languageRepository.save(language);
		
		captureHistory(savedLanguage, request.getLanguageId() == 0 ? "C" : "U");
		
		return savedLanguage;
	}
	
	private void captureHistory(Language language, String operation) {
		
		LanguageHistory languageHistory = new LanguageHistory();
		copyFromLanguage(language, languageHistory);
		
		languageHistory.setOperation(operation);
		
		languageHistoryRepository.save(languageHistory);
		
	}
	private void copyFromLanguage(Language language, LanguageHistory languageHistory) {
		
		languageHistory.setDescription(language.getDescription());
		languageHistory.setLanguageCode(language.getLanguageCode());
		languageHistory.setLanguageName(language.getLanguageName());
//		languageHistory.setRequesterIP(language.getRequesterIP());
		languageHistory.setLanguage(language);
	}

}
