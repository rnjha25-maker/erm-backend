package ermorg.erm.erm_command_organization.controller;

import ermorg.erm.erm_command_organization.dto.request.CityRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.CityResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.ICityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin
@RestController
@RequestMapping("city")
public class CityController {
    @Autowired
    private ICityService cityService;

    @PostMapping("/save")
    public GeneralResponse<Void> saveCities(@RequestBody CityRequest request) throws ResourceNotFoundException {
    	 GeneralResponse<Void> response = new GeneralResponse<>();
    	 
    	 cityService.saveCities(request);
    	 
    	 response.setStatus(ResponseStatus.SUCCESS);
    	 response.setMessage("Saved.");
    	 return response;
    }
    @GetMapping("/all/{id:[\\d]+}")
    public GeneralResponse<List<CityResponse>> getAllCities(@PathVariable Long id) {
        GeneralResponse<List<CityResponse>> response = new GeneralResponse<>();
        try{
            List<CityResponse> cities = cityService.getAllCities(id);
            response.setData(cities);
            response.setStatus(ResponseStatus.SUCCESS);
        }catch (ResourceNotFoundException e){
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
