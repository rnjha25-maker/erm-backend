package ermorg.erm.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ermorg.erm.dto.bulk.BulkImportSummary;
import ermorg.erm.dto.bulk.BulkImportUploadResponse;
import ermorg.erm.dto.response.SystemFieldResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.CompanyRepository;
import ermorg.erm.service.IBulkImportService;
import ermorg.erm.service.IFieldService;
import ermorg.erm.util.OrganizationContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BulkImportService implements IBulkImportService {

    private final IFieldService fieldService;
    private final CompanyRepository companyRepository;

   @Override
    public byte[] downloadTemplate(Long moduleId, String templateType) throws ResourceNotFoundException {
        String tableName = templateType != null && !templateType.isBlank() ? templateType : "company";
        try {
            SystemTableResponse table = fieldService.getSystemTableByName(tableName.toLowerCase());
            List<SystemFieldResponse> fields = table.getFields().stream()
                    .filter(f -> f.getIsHidden() == null || !f.getIsHidden())
                    .sorted((a, b) -> Integer.compare(
                            a.getDisplayOrder() == null ? 0 : a.getDisplayOrder(),
                            b.getDisplayOrder() == null ? 0 : b.getDisplayOrder()))
                    .toList();

            StringBuilder csv = new StringBuilder();
            List<String> headers = new ArrayList<>();
            headers.add("organizationName");
            for (SystemFieldResponse f : fields) {
                headers.add(f.getDisplayLabel() == null ? f.getField() : f.getDisplayLabel());
            }
            csv.append(String.join(",", headers)).append("\r\n");
            csv.append("My Company,Default Org,Active\r\n");
            return csv.toString().getBytes(StandardCharsets.UTF_8);
        } catch (ResourceNotFoundException ex) {
            StringBuilder csv = new StringBuilder();
            csv.append("companyName,organizationName,companyStatus,category,module\r\n");
            csv.append("Acme Ltd,Org 1,Active,Technology,1\r\n");
            return csv.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public BulkImportSummary validateImport(MultipartFile file, Long moduleId, String templateType) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ResourceNotFoundException("Please upload an Excel or CSV file.");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (!(name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".xlsx") || name.toLowerCase().endsWith(".xls"))) {
            throw new ResourceNotFoundException("Unsupported file type. Please upload CSV or Excel.");
        }

       List<String> lines = new ArrayList<>();
        if (name.toLowerCase().endsWith(".csv")) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            for (String line : content.split("\\r?\\n")) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } else {
            lines.add("organizationName,companyName,companyStatus");
        }

      BulkImportSummary summary = new BulkImportSummary();
        if (lines.size() < 2) {
            summary.getWarnings().add("No data rows were found in the uploaded file.");
            summary.setInvalidRows(1);
            summary.setTotalRows(1);
            return summary;
        }

     String headerLine = lines.get(0).trim();
        List<String> headers = List.of(headerLine.split(","));
        summary.setTotalRows(Math.max(1, lines.size() - 1));

     String tableName = templateType != null && !templateType.isBlank() ? templateType : "company";
        Map<String, SystemFieldResponse> headerFieldMap = new HashMap<>();
        List<SystemFieldResponse> tableFields = new ArrayList<>();
        try {
            SystemTableResponse table = fieldService.getSystemTableByName(tableName.toLowerCase());
            tableFields = table.getFields();
            Map<String, SystemFieldResponse> byLabel = tableFields.stream()
                    .collect(Collectors.toMap(f -> (f.getDisplayLabel() == null ? f.getField() : f.getDisplayLabel()).toLowerCase(), f -> f));
            Map<String, SystemFieldResponse> byField = tableFields.stream()
                    .collect(Collectors.toMap(f -> f.getField().toLowerCase(), f -> f));

         for (String h : headers) {
                String key = h.trim().toLowerCase();
                if (byLabel.containsKey(key)) {
                    headerFieldMap.put(h, byLabel.get(key));
                } else if (byField.containsKey(key)) {
                    headerFieldMap.put(h, byField.get(key));
                }
            }
            for (SystemFieldResponse sf : tableFields) {
                if (Boolean.TRUE.equals(sf.getIsRequired())) {
                    boolean present = headerFieldMap.values().stream().anyMatch(v -> Objects.equals(v.getField(), sf.getField()));
                    if (!present) {
                        summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(0, sf.getField(), "Required field '" + sf.getDisplayLabel() + "' is missing from template."));
                    }
                }
            }
        } catch (ResourceNotFoundException rnfe) {
            if (!headerLine.toLowerCase().contains("companyname") || !headerLine.toLowerCase().contains("organizationname")) {
                summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(1, "header", "Template headers do not match the required import format (companyName, organizationName required)."));
                summary.setInvalidRows(summary.getTotalRows());
                return summary;
            }
        }

        Organization currentOrg = OrganizationContext.getOrganization();
        Long currentOrgId = currentOrg == null ? null : currentOrg.getId();
        for (int rowIndex = 1; rowIndex < lines.size(); rowIndex++) {
            String row = lines.get(rowIndex);
            if (row.trim().isBlank()) {
                continue;
            }
            String[] cells = row.split(",", -1);
            if (cells.length < headers.size()) {
                summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(rowIndex + 1, "row", "Row has fewer columns than the template."));
                continue;
            }
            if (cells.length > headers.size()) {
                summary.getWarnings().add("Row " + (rowIndex + 1) + " contains extra columns and will be trimmed during processing.");
            }
            Map<String, String> rowMap = new HashMap<>();
            for (int c = 0; c < headers.size() && c < cells.length; c++) {
                rowMap.put(headers.get(c).trim(), cells[c].trim());
            }

            String companyName = null;
            if (rowMap.containsKey("companyName") || rowMap.containsKey("companyname")) {
                companyName = rowMap.getOrDefault("companyName", rowMap.get("companyname"));
            } else if (!headers.isEmpty()) {
                companyName = cells[0].trim();
            }
            if (companyName == null || companyName.isEmpty()) {
                summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(rowIndex + 1, "companyName", "Company name is required."));
            } else {
                if (currentOrgId != null) {
                    Optional<Company> existing = companyRepository.findByNameAndOrganizationIdAndDeletedFalse(companyName, currentOrgId);
                    if (existing.isPresent()) {
                        summary.getWarnings().add("Row " + (rowIndex + 1) + ": company '" + companyName + "' already exists in the current organization and will be skipped.");
                    }
                }
            }

            for (Map.Entry<String, SystemFieldResponse> entry : headerFieldMap.entrySet()) {
                String hdr = entry.getKey();
                SystemFieldResponse sf = entry.getValue();
                String value = rowMap.getOrDefault(hdr, "");
                if (Boolean.TRUE.equals(sf.getIsRequired()) && (value == null || value.isBlank())) {
                    summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(rowIndex + 1, sf.getField(), "Field '" + sf.getDisplayLabel() + "' is required."));
                }
                String ft = sf.getFieldType() == null ? "" : sf.getFieldType().toLowerCase();
                if (ft.contains("select") || ft.contains("dropdown") || ft.contains("option")) {
                    try {
                        List<SystemFieldResponse.FieldOptionResponse> options = fieldService.getFieldOptions(sf.getField(), tableName.toLowerCase());
                        boolean ok = options.stream().anyMatch(o -> o.getValue().equalsIgnoreCase(value) || o.getLabel().equalsIgnoreCase(value));
                        if (!value.isBlank() && !ok) {
                            summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(rowIndex + 1, sf.getField(), "Invalid value '" + value + "' for field '" + sf.getDisplayLabel() + "'."));
                        }
                    } catch (ResourceNotFoundException rnfe) {
                        // ignore — no options available
                    }
                }
                if (ft.contains("number") || ft.contains("int") || ft.contains("decimal")) {
                    if (!value.isBlank()) {
                        try {
                            Double.parseDouble(value);
                        } catch (NumberFormatException nfe) {
                            summary.getErrors().add(new ermorg.erm.dto.bulk.BulkImportValidationError(rowIndex + 1, sf.getField(), "Invalid numeric value '" + value + "'."));
                        }
                    }
                }
            }
        }

        summary.setValidRows(Math.max(0, summary.getTotalRows() - summary.getErrors().size()));
        summary.setInvalidRows(summary.getErrors().size());
        return summary;
    }

    @Override
    public BulkImportUploadResponse processImport(MultipartFile file, Long moduleId, String templateType) throws Exception {
        BulkImportSummary summary = validateImport(file, moduleId, templateType);
        BulkImportUploadResponse response = new BulkImportUploadResponse();
        response.setImportId(System.currentTimeMillis());
        response.setStatus("COMPLETED");
        response.setSummary(summary);

       if (summary.getErrors().isEmpty()) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<String> lines = new ArrayList<>();
            for (String line : content.split("\\r?\\n")) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
            if (lines.size() >= 2) {
                String headerLine = lines.get(0).trim();
                List<String> headers = List.of(headerLine.split(","));
                Organization currentOrg = OrganizationContext.getOrganization();
                Long currentOrgId = currentOrg == null ? null : currentOrg.getId();
                for (int i = 1; i < lines.size(); i++) {
                    String[] cells = lines.get(i).split(",", -1);
                    if (cells.length == 0) continue;
                    String companyName = cells[0].trim();
                    if (companyName.isBlank()) continue;
                    boolean skip = false;
                    if (currentOrgId != null) {
                        Optional<Company> existing = companyRepository.findByNameAndOrganizationIdAndDeletedFalse(companyName, currentOrgId);
                        if (existing.isPresent()) skip = true;
                    }
                    if (!skip) {
                        Company c = new Company();
                        c.setName(companyName);
                        if (currentOrg != null) c.setOrganization(currentOrg);
                        companyRepository.save(c);
                    }
                }
            }
        }

        return response;
    }

    @Override
    public BulkImportUploadResponse getImportStatus(Long importId) {
        BulkImportUploadResponse response = new BulkImportUploadResponse();
        response.setImportId(importId);
        response.setStatus("COMPLETED");
        response.setSummary(new BulkImportSummary());
        return response;
    }

    @Override
    public List<Object> getImportErrors(Long importId) {
        return new ArrayList<>();
    }
}
