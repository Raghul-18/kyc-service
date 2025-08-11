package com.bank.kyc.service;

import com.bank.kyc.dto.CustomerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    public CustomerDTO getCustomerById(Long customerId) {
        try {
            String url = customerServiceUrl + "/api/customers/" + customerId;
            ResponseEntity<CustomerDTO> response = restTemplate.getForEntity(url, CustomerDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch customer details: " + e.getMessage());
        }
    }

    public Page<CustomerDTO> getCustomersWithFilters(String search, String kycStatus, Pageable pageable) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(customerServiceUrl + "/api/customers/search")
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());

            if (search != null && !search.trim().isEmpty()) {
                builder.queryParam("search", search);
            }
            if (kycStatus != null && !kycStatus.trim().isEmpty()) {
                builder.queryParam("kycStatus", kycStatus);
            }

            ResponseEntity<CustomerDTO[]> response = restTemplate.getForEntity(
                    builder.toUriString(), CustomerDTO[].class);

            List<CustomerDTO> customers = Arrays.asList(response.getBody());

            // For simplicity, returning all results as single page
            // In production, implement proper pagination
            return new PageImpl<>(customers, pageable, customers.size());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch customers: " + e.getMessage());
        }
    }

    public void updateCustomerKYCStatus(Long customerId, String kycStatus) {
        try {
            String url = customerServiceUrl + "/api/customers/" + customerId + "/kyc-status";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            Map<String, String> requestBody = Map.of("kycStatus", kycStatus);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer KYC status: " + e.getMessage());
        }
    }

    public Long getCustomerIdByUserId(Long userId) {
        try {
            String url = customerServiceUrl + "/api/customers/user/" + userId + "/customer-id";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            return Long.valueOf(body.get("customerId").toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve customer ID: " + e.getMessage());
        }
    }
}