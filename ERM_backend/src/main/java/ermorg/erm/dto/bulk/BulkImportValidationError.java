package ermorg.erm.dto.bulk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportValidationError {
    private int rowNumber;
    private String columnName;
    private String message;
}
