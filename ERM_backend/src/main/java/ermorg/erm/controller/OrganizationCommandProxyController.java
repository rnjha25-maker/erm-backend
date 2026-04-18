package ermorg.erm.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxies read-only organization hierarchy calls to erm-command-organization so clients can use
 * gateway route {@code /erm/**} (StripPrefix) while CQRS data lives in the command service.
 */
@RestController
public class OrganizationCommandProxyController {

    private final RestTemplate restTemplate;

    @Value("${erm.command-organization.service-id:ERM-COMMAND-ORGANIZATION}")
    private String commandOrganizationServiceId;

    public OrganizationCommandProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/business-segment/get-all-by-department-id/{departmentId:[\\d]+}")
    public ResponseEntity<String> getBusinessSegmentsByDepartmentId(@PathVariable Long departmentId) {
        String url = String.format("http://%s/business-segment/get-all-by-department-id/%d",
                commandOrganizationServiceId, departmentId);
        return exchangeAsJson(url);
    }

    @GetMapping("/business-vertical/get-all-by-segment-id/{segmentId:[\\d]+}")
    public ResponseEntity<String> getBusinessVerticalsBySegmentId(@PathVariable Long segmentId) {
        String url = String.format("http://%s/business-vertical/get-all-by-segment-id/%d",
                commandOrganizationServiceId, segmentId);
        return exchangeAsJson(url);
    }

    private ResponseEntity<String> exchangeAsJson(String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }
}
