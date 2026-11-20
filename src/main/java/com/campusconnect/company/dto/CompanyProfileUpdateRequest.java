package com.campusconnect.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CompanyProfileUpdateRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String companyName;

    @NotBlank(message = "Industry is required")
    @Size(max = 100, message = "Industry cannot exceed 100 characters")
    private String industry;

    @Size(max = 200, message = "Website URL cannot exceed 200 characters")
    private String website;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Pattern(regexp = "^$|^[+0-9\\-\\s()]{7,20}$", message = "Invalid phone number format")
    private String contactNumber;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    public CompanyProfileUpdateRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
