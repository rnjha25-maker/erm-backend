package ermorg.erm.dto.response;

import ermorg.erm.model.State;

import lombok.Data;

@Data
public class StateResponse {
    private Long id;
    private String stateName;

    public StateResponse(State state) {
        this.id = state.getId();
        this.stateName = state.getName();
    }
}
