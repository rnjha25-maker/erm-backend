package ermorg.erm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.riskDTO.LanguageRequest;
import ermorg.erm.model.translation.Language;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.ILanguageService;

@RestController
@RequestMapping("/language")
public class LanguageController {

	@Autowired
	private ILanguageService languageService;

	@PostMapping("/save")
	public GeneralResponse<Language> saveLanguage(@RequestBody LanguageRequest request) {
		GeneralResponse<Language> response = new GeneralResponse<>();

		Language savedLanguage = languageService.saveLanguage(request);

		response.setData(savedLanguage);
		response.setMessage("Language saved.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

}
