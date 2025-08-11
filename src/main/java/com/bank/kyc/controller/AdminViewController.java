package com.bank.kyc.controller;

import com.bank.kyc.dto.CustomerDTO;
import com.bank.kyc.dto.KycStatsDTO;
import com.bank.kyc.entity.KycDocument;
import com.bank.kyc.service.AdminDashboardService;
import com.bank.kyc.service.CustomerIntegrationService;
import com.bank.kyc.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private CustomerIntegrationService customerIntegrationService;

    @Autowired
    private KycService kycDocumentService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            HttpServletRequest request) {

        // Validate admin authentication
        if (!isAdminAuthenticated(request)) {
            return "redirect:/admin/login";
        }

        try {
            // Get KYC Statistics
            KycStatsDTO kycStats = adminDashboardService.getKYCStatistics();
            model.addAttribute("kycStats", kycStats);

            // Get customers with filtering
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomerDTO> customersPage = customerIntegrationService.getCustomersWithFilters(
                    search, kycStatus, pageable);

            model.addAttribute("customers", customersPage.getContent());
            model.addAttribute("totalPages", customersPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("search", search);
            model.addAttribute("kycStatus", kycStatus);

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard data");
        }

        return "admin/dashboard";
    }

    @GetMapping("/customer/{customerId}")
    public String showCustomerDetails(
            @PathVariable Long customerId,
            Model model,
            HttpServletRequest request) {

        // Validate admin authentication
        if (!isAdminAuthenticated(request)) {
            return "redirect:/admin/login";
        }

        try {
            // Get customer details
            CustomerDTO customer = customerIntegrationService.getCustomerById(customerId);
            if (customer == null) {
                model.addAttribute("error", "Customer not found");
                return "admin/dashboard";
            }

            // Get customer's KYC documents
            List<KycDocument> documents = kycDocumentService.getDocumentsByCustomerId(customerId);

            // Create document map for easy access in template
            Map<String, KycDocument> documentMap = documents.stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getDocumentType().toString(),
                            doc -> doc,
                            (existing, replacement) -> replacement
                    ));

            // Check if all required documents are verified
            boolean allDocumentsVerified = isAllDocumentsVerified(documentMap);

            model.addAttribute("customer", customer);
            model.addAttribute("documentMap", documentMap);
            model.addAttribute("allDocumentsVerified", allDocumentsVerified);

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load customer details");
            return "admin/dashboard";
        }

        return "admin/customer-details";
    }

    private boolean isAdminAuthenticated(HttpServletRequest request) {
        // This would be implemented based on your JWT validation logic
        String token = extractTokenFromRequest(request);
        return token != null; // Simplified check - implement proper JWT validation
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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
}