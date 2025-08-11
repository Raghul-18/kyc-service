package com.bank.kyc.service;

import com.bank.kyc.dto.CustomerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CustomerIntegrationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${customer.service.url:http://localhost:8081}")
    private String customerServiceUrl;

    private HttpHeaders createHeadersWithJwt(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null && !jwtToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + jwtToken);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public CustomerDTO getCustomerById(Long customerId, String jwtToken) {
        try {
            String url = customerServiceUrl + "/api/customers/" + customerId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithJwt(jwtToken));
            ResponseEntity<CustomerDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, CustomerDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch customer details: " + e.getMessage());
        }
    }

    // Use admin endpoint /api/customers/admin/all with pagination and optional filters (search, kycStatus)
    public Page<CustomerDTO> getCustomersWithFilters(String search, String kycStatus, Pageable pageable, String jwtToken) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(customerServiceUrl + "/api/customers/admin/all")
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());

            if (search != null && !search.trim().isEmpty()) {
                builder.queryParam("search", search);
            }
            if (kycStatus != null && !kycStatus.trim().isEmpty()) {
                builder.queryParam("kycStatus", kycStatus);
            }

            HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithJwt(jwtToken));
            ResponseEntity<CustomerDTO[]> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, CustomerDTO[].class);

            List<CustomerDTO> customers = Arrays.asList(response.getBody());

            // Note: total count should ideally come from response headers or separate API
            return new PageImpl<>(customers, pageable, customers.size());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch customers: " + e.getMessage());
        }
    }

    // Use admin endpoint for updating KYC status
    public void updateCustomerKYCStatus(Long customerId, String kycStatus, String jwtToken) {
        try {
            String url = customerServiceUrl + "/api/customers/admin/" + customerId + "/kyc-status";

            Map<String, String> requestBody = Map.of("kycStatus", kycStatus);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, createHeadersWithJwt(jwtToken));

            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer KYC status: " + e.getMessage());
        }
    }

    public Long getCustomerIdByUserId(Long userId, String jwtToken) {
        try {
            String url = customerServiceUrl + "/api/customers/user/" + userId + "/customer-id";
            HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithJwt(jwtToken));
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();
            return Long.valueOf(body.get("customerId").toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve customer ID: " + e.getMessage());
        }
    }
}
