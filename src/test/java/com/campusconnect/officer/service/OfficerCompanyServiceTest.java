package com.campusconnect.officer.service;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerCompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private OfficerCompanyService officerCompanyService;

    private Company company;

    @BeforeEach
    void setUp() {
        User recruiter = new User();
        recruiter.setId(15L);
        recruiter.setFullName("Marcus Vance");
        recruiter.setEmail("marcus@nexus.io");
        recruiter.setRole(Role.RECRUITER);

        company = new Company();
        company.setId(200L);
        company.setRecruiter(recruiter);
        company.setCompanyName("Nexus Innovations");
        company.setIndustry("FinTech");
        company.setApprovalStatus(CompanyApprovalStatus.PENDING);
    }

    @Test
    void getCompanies_CallsRepositorySearch() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Company> page = new PageImpl<>(List.of(company));
        when(companyRepository.searchCompanies(CompanyApprovalStatus.PENDING, "Nexus", pageable))
                .thenReturn(page);

        Page<Company> result = officerCompanyService.getCompanies(CompanyApprovalStatus.PENDING, "Nexus", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getPendingCount_ReturnsCount() {
        when(companyRepository.countByApprovalStatus(CompanyApprovalStatus.PENDING)).thenReturn(3L);

        long count = officerCompanyService.getPendingCount();

        assertEquals(3L, count);
    }

    @Test
    void approveCompany_ValidId_SetsStatusApprovedAndOfficerMetadata() {
        when(companyRepository.findByIdWithRecruiter(200L)).thenReturn(Optional.of(company));

        officerCompanyService.approveCompany(200L, "officer@campusconnect.edu");

        assertEquals(CompanyApprovalStatus.APPROVED, company.getApprovalStatus());
        assertEquals("officer@campusconnect.edu", company.getDecisionBy());
        assertNotNull(company.getDecisionAt());
        assertNull(company.getRejectionReason());
        verify(companyRepository).save(company);
    }

    @Test
    void rejectCompany_ValidReason_SetsStatusRejectedAndReason() {
        when(companyRepository.findByIdWithRecruiter(200L)).thenReturn(Optional.of(company));

        officerCompanyService.rejectCompany(200L, "Corporate credentials could not be verified.", "officer@campusconnect.edu");

        assertEquals(CompanyApprovalStatus.REJECTED, company.getApprovalStatus());
        assertEquals("Corporate credentials could not be verified.", company.getRejectionReason());
        assertEquals("officer@campusconnect.edu", company.getDecisionBy());
        assertNotNull(company.getDecisionAt());
        verify(companyRepository).save(company);
    }

    @Test
    void rejectCompany_BlankReason_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                officerCompanyService.rejectCompany(200L, "   ", "officer@campusconnect.edu")
        );

        assertEquals("A rejection reason must be provided.", ex.getMessage());
    }

    @Test
    void getCompanyById_NotFound_ThrowsException() {
        when(companyRepository.findByIdWithRecruiter(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                officerCompanyService.getCompanyById(999L)
        );
    }
}
