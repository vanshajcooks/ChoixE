package com.campusconnect.company.service;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.dto.CompanyProfileUpdateRequest;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private User recruiter;
    private Company company;

    @BeforeEach
    void setUp() {
        recruiter = new User();
        recruiter.setId(10L);
        recruiter.setFullName("Sarah Connor");
        recruiter.setEmail("sarah@techcorp.com");
        recruiter.setRole(Role.RECRUITER);

        company = new Company();
        company.setId(100L);
        company.setRecruiter(recruiter);
        company.setCompanyName("TechCorp Global");
        company.setIndustry("Technology");
        company.setWebsite("https://techcorp.com");
        company.setLocation("San Francisco, CA");
        company.setContactNumber("+1 555-0199");
        company.setDescription("Global tech solutions provider");
        company.setApprovalStatus(CompanyApprovalStatus.PENDING);
    }

    @Test
    void getCompanyByRecruiterEmail_Found_ReturnsCompany() {
        when(companyRepository.findByRecruiterEmail("sarah@techcorp.com"))
                .thenReturn(Optional.of(company));

        Company found = companyService.getCompanyByRecruiterEmail("sarah@techcorp.com");

        assertNotNull(found);
        assertEquals("TechCorp Global", found.getCompanyName());
        assertEquals("Technology", found.getIndustry());
    }

    @Test
    void getCompanyByRecruiterEmail_NotFound_ThrowsException() {
        when(companyRepository.findByRecruiterEmail("unknown@corp.com"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                companyService.getCompanyByRecruiterEmail("unknown@corp.com")
        );
    }

    @Test
    void updateCompanyProfile_ValidRequest_UpdatesFieldsAndSaves() {
        when(companyRepository.findByRecruiterEmail("sarah@techcorp.com"))
                .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompanyProfileUpdateRequest request = new CompanyProfileUpdateRequest();
        request.setCompanyName("TechCorp Innovations");
        request.setIndustry("Artificial Intelligence");
        request.setWebsite("https://techcorp-ai.com");
        request.setLocation("Seattle, WA");
        request.setContactNumber("+1 555-0200");
        request.setDescription("Pioneering cutting edge intelligence");

        Company updated = companyService.updateCompanyProfile("sarah@techcorp.com", request);

        assertEquals("TechCorp Innovations", updated.getCompanyName());
        assertEquals("Artificial Intelligence", updated.getIndustry());
        assertEquals("https://techcorp-ai.com", updated.getWebsite());
        assertEquals("Seattle, WA", updated.getLocation());
        assertEquals("+1 555-0200", updated.getContactNumber());
        assertEquals("Pioneering cutting edge intelligence", updated.getDescription());
        verify(companyRepository).save(company);
    }
}
