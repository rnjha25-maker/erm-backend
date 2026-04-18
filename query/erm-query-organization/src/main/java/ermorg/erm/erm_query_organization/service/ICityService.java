package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.City;

import java.util.List;

public interface ICityService {
    List<City> getAllCity(Long stateId) throws DataNotFoundException;
}
