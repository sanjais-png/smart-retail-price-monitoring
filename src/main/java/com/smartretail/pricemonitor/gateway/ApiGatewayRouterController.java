package com.smartretail.pricemonitor.gateway;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/gateway")
@Slf4j
public class ApiGatewayRouterController {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String AUTH_SERVICE_URL = "http://localhost:8081";
    private static final String LOCATION_SERVICE_URL = "http://localhost:8082";
    private static final String CATALOG_SERVICE_URL = "http://localhost:8083";
    private static final String FAIRNESS_SERVICE_URL = "http://localhost:8084";
    private static final String AGMARKNET_SERVICE_URL = "http://localhost:8085";
    private static final String AI_GOVERNANCE_SERVICE_URL = "http://localhost:8086";

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> routeRequest(@RequestBody(required = false) byte[] body,
                                         HttpServletRequest request) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetBaseUrl = resolveTargetServiceUrl(path);

        String targetUrl = targetBaseUrl + path + (queryString != null ? "?" + queryString : "");
        log.info("API Gateway Routing: {} {} -> {}", request.getMethod(), path, targetUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            });

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(new URI(targetUrl), method, httpEntity, byte[].class);
        } catch (HttpStatusCodeException e) {
            log.error("Gateway Microservice error response [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("Gateway error routing request to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("success", false, "message", "API Gateway Error: Microservice unavailable (" + e.getMessage() + ")"));
        }
    }

    private String resolveTargetServiceUrl(String path) {
        if (path.startsWith("/api/v1/auth")) {
            return AUTH_SERVICE_URL;
        } else if (path.startsWith("/api/v1/locations") || path.startsWith("/api/v1/markets")) {
            return LOCATION_SERVICE_URL;
        } else if (path.startsWith("/api/v1/categories") || path.startsWith("/api/v1/commodities")) {
            return CATALOG_SERVICE_URL;
        } else if (path.startsWith("/api/v1/fairness") || path.startsWith("/api/v1/reports")) {
            return FAIRNESS_SERVICE_URL;
        } else if (path.startsWith("/api/v1/datasets")) {
            return AGMARKNET_SERVICE_URL;
        } else if (path.startsWith("/api/v1/ai") || path.startsWith("/api/v1/complaints") || path.startsWith("/api/v1/alerts")) {
            return AI_GOVERNANCE_SERVICE_URL;
        }
        return "http://localhost:8090"; // Local fallback
    }
}
