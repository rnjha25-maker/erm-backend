package ermorg.erm.dto.bulk;

import lombok.Data;

@Data
public class BulkImportUploadResponse {
    private Long importId;
    private String status;
    private BulkImportSummary summary;
}
