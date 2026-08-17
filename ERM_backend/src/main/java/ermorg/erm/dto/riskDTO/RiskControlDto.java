package ermorg.erm.dto.riskDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ermorg.erm.constant.RiskAcceptanceLevel;
import lombok.Data;

@Data
public class RiskControlDto {

	private long riskControlId;
	private long riskId;

    /**
     * Accepts both the canonical JSON array form (subRiskIds) and the legacy
     * comma-separated string form that older frontend versions send as "riskSubs".
     * Examples that all map correctly:
     *   "subRiskIds": [63252, 63253]       ← preferred array
     *   "subRiskIds": "63252,63253"        ← legacy string
     *   "riskSubs":   "63252"              ← legacy field name + string
     *   "riskSubs":   [63252]              ← legacy field name + array
     */
    @JsonAlias("riskSubs")
    @JsonDeserialize(using = LongListDeserializer.class)
    private List<Long> subRiskIds = new ArrayList<>();

    private String controlSource;
    private String controlTitle;
    private List<RiskSubControlDto> controlSubTitle;
    private String controlType;
    private String controlMechanism;
    private String accessControls;
    private String processControls;
    private String physicalControls;
    private String technicalControl;
    private String controlAssessmentFrequency;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private long primaryResponsible;
    private long approver;
    private Date actualDate;
    private Date lastControlAssessmentDate;
    private Date nextControlAssessmentDate;
}

/**
 * Handles three wire formats for a list of Long IDs:
 *   1. JSON array:           [63252, 63253]
 *   2. Comma-separated str:  "63252,63253"
 *   3. Single numeric str:   "63252"
 *   4. Single number node:   63252
 * Returns an empty list for null / blank input.
 */
class LongListDeserializer extends JsonDeserializer<List<Long>> {
    @Override
    public List<Long> deserialize(JsonParser p, DeserializationContext ctx)
            throws java.io.IOException {
        com.fasterxml.jackson.databind.JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) {
            return new ArrayList<>();
        }
        if (node instanceof ArrayNode arr) {
            List<Long> result = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode element : arr) {
                if (!element.isNull()) {
                    try { result.add(element.asLong()); } catch (Exception ignored) {}
                }
            }
            return result;
        }
        if (node instanceof TextNode || node.isNumber()) {
            String raw = node.asText("").trim();
            if (raw.isBlank()) return new ArrayList<>();
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && s.matches("-?\\d+"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
