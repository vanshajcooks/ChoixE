package com.campusconnect.officer.service;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OfficerCompanyService {

    private final CompanyRepository companyRepository;

    public OfficerCompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public Page<Company> getCompanies(CompanyApprovalStatus status, String search, Pageable pageable) {
        return companyRepository.searchCompanies(status, search, pageable);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return companyRepository.countByApprovalStatus(CompanyApprovalStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findByIdWithRecruiter(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + id));
    }

    @Transactional
    public void approveCompany(Long id, String officerEmail) {
        Company company = getCompanyById(id);
        company.setApprovalStatus(CompanyApprovalStatus.APPROVED);
        company.setDecisionAt(LocalDateTime.now());
        company.setDecisionBy(officerEmail);
        company.setRejectionReason(null);
        companyRepository.save(company);
    }

    @Transactional
    public void rejectCompany(Long id, String reason, String officerEmail) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason must be provided.");
        }
        Company company = getCompanyById(id);
        company.setApprovalStatus(CompanyApprovalStatus.REJECTED);
        company.setRejectionReason(reason.trim());
        company.setDecisionAt(LocalDateTime.now());
        company.setDecisionBy(officerEmail);
        companyRepository.save(company);
    }
}
