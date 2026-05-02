package ermorg.erm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamedCount {
	private String key;
	private long count;
	private String displayLabel;
}
