package ermorg.erm.util;

import java.util.Map;
import org.springframework.web.client.RestTemplate;

/** Shared storage transport. Callers retain their validation and error contracts. */
public final class StorageDocumentClient {
    private final RestTemplate http;
    private final String baseUrl;

    public StorageDocumentClient(RestTemplate http, String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> upload(Map<String, Object> request) {
        return http.postForObject(baseUrl + "/upload", request, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> download(String documentId) {
        return http.getForObject(baseUrl + "/download/" + documentId, Map.class);
    }

    public void delete(String documentId) {
        http.delete(baseUrl + "/delete/" + documentId);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> dataOrNull(Map<?, ?> response) {
        Object data = response == null ? null : response.get("data");
        return data instanceof Map<?, ?> ? (Map<String, Object>) data : null;
    }
}
