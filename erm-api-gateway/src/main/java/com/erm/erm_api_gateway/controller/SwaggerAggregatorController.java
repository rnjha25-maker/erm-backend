package com.erm.erm_api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.erm.erm_api_gateway.util.IPResolver;

@RestController
@RequestMapping("/swagger")
public class SwaggerAggregatorController {

    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/{service}/v3/api-docs")
    public ResponseEntity<?> getSwaggerDocs(@PathVariable String service) {
        String ip = IPResolver.getHostIP();

        String targetUrl = switch (service) {
            case "org" -> "http://" + ip + ":8081/v3/api-docs";
            case "order-service" -> "http://" + ip + ":8082/v3/api-docs";
            default -> null;
        };

        if (targetUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unknown service: " + service);
        }

        try {
            // Using WebClient for non-blocking async calls
            String body = webClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Convert to blocking for backward compatibility, or use reactive endpoints
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching Swagger docs: " + e.getMessage());
        }
    }
}


