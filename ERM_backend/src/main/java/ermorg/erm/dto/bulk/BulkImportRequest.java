package ermorg.erm.dto.bulk;

import java.util.List;

import lombok.Data;

@Data
public class BulkImportRequest {
    private Long companyId;
    private Long moduleId;
    private String templateType;
    private List<String> headers;
    private List<List<String>> rows;
}
