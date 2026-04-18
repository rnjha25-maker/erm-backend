package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.State;

import java.util.List;

public interface IStateService {
    List<State> getAllState(Long countryId) throws DataNotFoundException;
}
