package ermorg.erm.dto.bulk;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BulkImportSummary {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int duplicateRows;
    private List<String> warnings = new ArrayList<>();
    private List<BulkImportValidationError> errors = new ArrayList<>();
}
