package com.bank.kyc.controller;

import com.bank.kyc.dto.CustomerDTO;
import com.bank.kyc.dto.KycStatsDTO;
import com.bank.kyc.entity.KycDocument;
import com.bank.kyc.entity.User;
import com.bank.kyc.service.AdminDashboardService;
import com.bank.kyc.service.CustomerIntegrationService;
import com.bank.kyc.service.KycService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private CustomerIntegrationService customerIntegrationService;

    @Autowired
    private KycService kycDocumentService;

    private User getCurrentUser() {
        try {
            return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("ADMIN_JWT".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            HttpServletRequest request) {

        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/admin/login";
        }

        KycStatsDTO kycStats = createDefaultKycStats();
        try {
            KycStatsDTO realStats = adminDashboardService.getKYCStatistics();
            if (realStats != null) {
                kycStats = realStats;
            }
        } catch (Exception e) {
            log.error("Failed to load KYC statistics, using defaults", e);
            model.addAttribute("statsError", "Unable to load statistics");
        }
        model.addAttribute("kycStats", kycStats);

        try {
            Pageable pageable = PageRequest.of(page, size);

            String jwtToken = extractJwtFromRequest(request);

            Page<CustomerDTO> customersPage = customerIntegrationService.getCustomersWithFilters(
                    search, kycStatus, pageable, jwtToken);

            model.addAttribute("customers", customersPage.getContent());
            model.addAttribute("totalPages", customersPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("search", search);
            model.addAttribute("kycStatus", kycStatus);

        } catch (Exception e) {
            log.error("Failed to load customer data", e);
            model.addAttribute("error", "Failed to load customer data");
            model.addAttribute("customers", Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
        }

        return "admin/dashboard";
    }

    @GetMapping("/customer/{customerId}")
    public String showCustomerDetails(
            @PathVariable Long customerId,
            Model model,
            HttpServletRequest request) {

        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/admin/login";
        }

        try {
            String jwtToken = extractJwtFromRequest(request);

            CustomerDTO customer = customerIntegrationService.getCustomerById(customerId, jwtToken);
            if (customer == null) {
                model.addAttribute("error", "Customer not found");
                return "admin/dashboard";
            }

            List<KycDocument> documents = kycDocumentService.getDocumentsByCustomerId(customerId);

            Map<String, KycDocument> documentMap = documents.stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getDocumentType().toString(),
                            doc -> doc,
                            (existing, replacement) -> replacement
                    ));

            boolean allDocumentsVerified = isAllDocumentsVerified(documentMap);

            model.addAttribute("customer", customer);
            model.addAttribute("documentMap", documentMap);
            model.addAttribute("allDocumentsVerified", allDocumentsVerified);

        } catch (Exception e) {
            log.error("Failed to load customer details for customerId: {}", customerId, e);
            model.addAttribute("error", "Failed to load customer details");
            return "admin/dashboard";
        }

        return "admin/customer-details";
    }

    private boolean isAllDocumentsVerified(Map<String, KycDocument> documentMap) {
        String[] requiredTypes = {"AADHAR", "PAN", "PHOTO"};
        for (String type : requiredTypes) {
            KycDocument doc = documentMap.get(type);
            if (doc == null || !doc.getStatus().toString().equals("VERIFIED")) {
                return false;
            }
        }
        return true;
    }

    private KycStatsDTO createDefaultKycStats() {
        return KycStatsDTO.builder()
                .total(0L)
                .pending(0L)
                .verified(0L)
                .rejected(0L)
                .build();
    }
}
