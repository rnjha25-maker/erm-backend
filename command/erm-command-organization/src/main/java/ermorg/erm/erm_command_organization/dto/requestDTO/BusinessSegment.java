package ermorg.erm.erm_command_organization.dto.requestDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BusinessSegment {
    private long businessSegmentId;

    @JsonProperty("segmentName")
    @JsonAlias({ "businessSegmentName" })
    private String segmentName;

    @JsonProperty("segmentCode")
    @JsonAlias({ "businessSegmentCode" })
    private String segmentCode;

    private String description;
}
