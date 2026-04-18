package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.State;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StateResponse {
    private Long id;
    private String stateName;

    public StateResponse(State state) {
        this.id = state.getId();
        this.stateName = state.getName();
    }
}
