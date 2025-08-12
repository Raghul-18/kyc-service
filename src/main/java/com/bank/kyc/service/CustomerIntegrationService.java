package com.bank.kyc.service;

import com.bank.kyc.dto.CustomerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(CustomerIntegrationService.class);
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
            String url = customerServiceUrl + "/api/customers/admin/" + customerId;
            log.debug("Calling Customer Service: {}", url);

            HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithJwt(jwtToken));
            ResponseEntity<CustomerDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, CustomerDTO.class);

            CustomerDTO customer = response.getBody();
            if (customer != null) {
                log.info("Fetched customer: ID={}, KYC status={}", customerId, customer.getKycStatus());
            } else {
                log.warn("Customer with ID {} not found", customerId);
            }
            return customer;

        } catch (Exception e) {
            log.error("Customer Service Error: {}", e.getMessage(), e);
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
//    public void updateCustomerKYCStatus(Long customerId, String kycStatus, String jwtToken) {
//        try {
//            String url = customerServiceUrl + "/api/customers/admin/" + customerId + "/kyc-status";
//
//            Map<String, String> requestBody = Map.of("kycStatus", kycStatus);
//            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, createHeadersWithJwt(jwtToken));
//
//            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to update customer KYC status: " + e.getMessage());
//        }
//    }
    public void updateCustomerKYCStatus(Long customerId, String kycStatus, String jwtToken) {
        try {
            String url = customerServiceUrl + "/api/customers/admin/" + customerId + "/kyc-status";
            Map<String, String> requestBody = Map.of("kycStatus", kycStatus);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, createHeadersWithJwt(jwtToken));

            System.out.println("➡️ Sending PUT request to update KYC status: " + url);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            System.out.println("⬅️ Received response status: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("❌ Failed to update customer KYC status: " + e.getMessage());
            throw new RuntimeException("Failed to update customer KYC status: " + e.getMessage());
        }
    }


    public void approveCustomerKYC(Long customerId, String jwtToken) {
        try {
            // Build the correct final URL
            String url = customerServiceUrl + "/api/customers/" + customerId + "/kyc-status?status=VERIFIED";

            HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithJwt(jwtToken));

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            System.out.println("✅ Customer KYC approved for customerId: " + customerId);
        } catch (Exception e) {
            System.err.println("❌ Failed to approve customer KYC: " + e.getMessage());
            throw new RuntimeException("Failed to approve customer KYC: " + e.getMessage());
        }
    }

}