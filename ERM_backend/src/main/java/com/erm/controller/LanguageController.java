package com.erm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.dto.ResponseStatus;
import com.erm.dto.riskDTO.LanguageRequest;
import com.erm.model.translation.Language;
import com.erm.response.GeneralResponse;
import com.erm.service.ILanguageService;

@RestController
@RequestMapping("/language")
public class LanguageController {
	
	@Autowired
	private ILanguageService languageService;
	
	@PostMapping("/save")
	public GeneralResponse<Language> saveLanguage(@RequestBody LanguageRequest request){
		GeneralResponse<Language> response = new GeneralResponse<>();
		
		Language savedLanguage = languageService.saveLanguage(request);
		
		response.setData(savedLanguage);
		response.setMessage("Language saved.");
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}

}
