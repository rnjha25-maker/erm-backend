package ermorg.erm.evaluation;

import java.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ermorg.erm.util.*;

@Component
public class PeriodEvidenceClient {
    private final StorageDocumentClient storage;

    public PeriodEvidenceClient(@Qualifier("periodStorageRestTemplate") RestTemplate storage,
            @Value("${erm.storage-service-url:http://storage:8085}") String url) {
        this.storage = new StorageDocumentClient(storage, url);
    }
    public String upload(MultipartFile file) {
        if(file==null||file.isEmpty()||file.getSize()>10*1024*1024)throw new IllegalArgumentException("Evidence must be nonempty and at most 10MB.");
        String name=Objects.toString(file.getOriginalFilename(),"evidence.bin").replace('\\','/');
        name=name.substring(name.lastIndexOf('/')+1);int dot=name.lastIndexOf('.');
        var body=new HashMap<String,Object>();body.put("fileName",dot>0?name.substring(0,dot):name);
        body.put("fileExtension",dot>0?name.substring(dot):".bin");body.put("contentType",Objects.toString(file.getContentType(),"application/octet-stream"));
        body.put("purpose","period-evaluation");body.put("organizationId",OrganizationContext.getOrganization().getId());body.put("companyId",CompanyContext.getCompany().getId());
        try {body.put("fileContent",Base64.getEncoder().encodeToString(file.getBytes()));}
        catch(java.io.IOException ex){throw new IllegalArgumentException("Unable to read evidence file.",ex);}
        Map<?,?> data=data(storage.upload(body));
        Object id=data.get("documentId");if(id==null)id=data.get("id");
        if(id==null)throw new IllegalStateException("Storage returned no document ID.");
        return UUID.fromString(id.toString()).toString();
    }
    public Map<?,?> download(String id) {return data(storage.download(UUID.fromString(id).toString()));}
    private Map<?,?> data(Map<?,?> response) {
        Map<String, Object> result = StorageDocumentClient.dataOrNull(response);
        if(result==null)throw new IllegalStateException("Invalid storage response.");
        return result;
    }
}
