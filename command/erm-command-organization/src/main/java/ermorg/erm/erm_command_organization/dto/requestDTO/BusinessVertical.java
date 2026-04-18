package ermorg.erm.erm_command_organization.dto.requestDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BusinessVertical {
    private long businessVerticalId;

    @JsonProperty("verticalName")
    @JsonAlias({ "businessVerticalName" })
    private String verticalName;

    @JsonProperty("verticalCode")
    @JsonAlias({ "businessVerticalCode" })
    private String verticalCode;

    private String description;
}
