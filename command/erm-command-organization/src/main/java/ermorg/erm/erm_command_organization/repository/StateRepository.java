package ermorg.erm.erm_command_organization.repository;

import ermorg.erm.erm_command_organization.dto.responseDTO.StateResponse;
import ermorg.erm.erm_command_organization.model.State;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends CrudRepository<State, Long> {
    State save(State state);
}
