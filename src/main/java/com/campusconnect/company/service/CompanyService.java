package com.campusconnect.company.service;

import com.campusconnect.company.dto.CompanyProfileUpdateRequest;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public Company getCompanyByRecruiterEmail(String email) {
        return companyRepository.findByRecruiterEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Company profile not found for recruiter email: " + email));
    }

    @Transactional
    public Company updateCompanyProfile(String email, CompanyProfileUpdateRequest request) {
        Company company = getCompanyByRecruiterEmail(email);

        company.setCompanyName(request.getCompanyName().trim());
        company.setIndustry(request.getIndustry().trim());
        company.setWebsite(request.getWebsite() != null ? request.getWebsite().trim() : null);
        company.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        company.setContactNumber(request.getContactNumber() != null ? request.getContactNumber().trim() : null);
        company.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        return companyRepository.save(company);
    }
}
