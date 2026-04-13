package com.erm.erm_command_organization.controller;

import com.erm.erm_command_organization.dto.responseDTO.CountryResponse;
import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_command_organization.service.ICountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@CrossOrigin
@RestController
@RequestMapping("country")
public class CountryController {
    @Autowired
    private ICountryService countryService;

    @GetMapping("/all")
    public GeneralResponse<List<CountryResponse>> getAllCountry() {
        GeneralResponse<List<CountryResponse>> response = new GeneralResponse<>();
        List<CountryResponse> allCountry = countryService.getAllCountry();
        response.setData(allCountry);
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }
}
