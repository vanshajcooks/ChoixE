package com.campusconnect.analytics.service;

import com.campusconnect.analytics.dto.*;
import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.repository.ApplicationRepository;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.repository.JobRepository;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacementAnalyticsServiceTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PlacementAnalyticsService placementAnalyticsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getOverviewStats_NormalData() {
        when(studentProfileRepository.count()).thenReturn(100L);
        when(studentProfileRepository.countByVerificationStatus(VerificationStatus.VERIFIED)).thenReturn(80L);
        when(applicationRepository.countDistinctPlacedStudents(ApplicationStatus.SELECTED)).thenReturn(64L);
        when(applicationRepository.count()).thenReturn(250L);
        when(applicationRepository.countByStatus(ApplicationStatus.SELECTED)).thenReturn(75L);
        when(jobRepository.countByStatus(JobStatus.APPROVED)).thenReturn(15L);
        when(companyRepository.countByApprovalStatus(CompanyApprovalStatus.APPROVED)).thenReturn(8L);

        PlacementOverviewStats stats = placementAnalyticsService.getOverviewStats();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalStudents());
        assertEquals(80L, stats.getVerifiedStudents());
        assertEquals(64L, stats.getPlacedStudents());
        assertEquals(80.0, stats.getPlacementPercentage()); // (64 / 80) * 100 = 80.0%
        assertEquals(64.0, stats.getOverallPlacementPercentage()); // (64 / 100) * 100 = 64.0%
        assertEquals(250L, stats.getTotalApplications());
        assertEquals(75L, stats.getTotalOffers());
        assertEquals(15L, stats.getTotalApprovedJobs());
        assertEquals(8L, stats.getTotalApprovedCompanies());
    }

    @Test
    void getOverviewStats_ZeroStudents_HandlesDivisionByZeroGracefully() {
        when(studentProfileRepository.count()).thenReturn(0L);
        when(studentProfileRepository.countByVerificationStatus(VerificationStatus.VERIFIED)).thenReturn(0L);
        when(applicationRepository.countDistinctPlacedStudents(ApplicationStatus.SELECTED)).thenReturn(0L);
        when(applicationRepository.count()).thenReturn(0L);
        when(applicationRepository.countByStatus(ApplicationStatus.SELECTED)).thenReturn(0L);
        when(jobRepository.countByStatus(JobStatus.APPROVED)).thenReturn(0L);
        when(companyRepository.countByApprovalStatus(CompanyApprovalStatus.APPROVED)).thenReturn(0L);

        PlacementOverviewStats stats = placementAnalyticsService.getOverviewStats();

        assertNotNull(stats);
        assertEquals(0.0, stats.getPlacementPercentage());
        assertEquals(0.0, stats.getOverallPlacementPercentage());
    }

    @Test
    void getDepartmentStats_CombinesCountsAndComputesRates() {
        List<Object[]> deptCounts = new ArrayList<>();
        deptCounts.add(new Object[]{"Computer Science", 50L, 40L});
        deptCounts.add(new Object[]{"Information Technology", 30L, 20L});

        List<Object[]> placedCounts = new ArrayList<>();
        placedCounts.add(new Object[]{"Computer Science", 32L});
        placedCounts.add(new Object[]{"Information Technology", 10L});

        when(studentProfileRepository.findDepartmentStudentCounts()).thenReturn(deptCounts);
        when(applicationRepository.findPlacedStudentCountByDepartment(ApplicationStatus.SELECTED)).thenReturn(placedCounts);

        List<DepartmentPlacementStat> stats = placementAnalyticsService.getDepartmentStats();

        assertNotNull(stats);
        assertEquals(2, stats.size());

        DepartmentPlacementStat cs = stats.get(0);
        assertEquals("Computer Science", cs.getDepartmentName());
        assertEquals(50L, cs.getTotalStudents());
        assertEquals(40L, cs.getVerifiedStudents());
        assertEquals(32L, cs.getPlacedStudents());
        assertEquals(80.0, cs.getPlacementPercentage()); // (32 / 40) * 100

        DepartmentPlacementStat it = stats.get(1);
        assertEquals("Information Technology", it.getDepartmentName());
        assertEquals(30L, it.getTotalStudents());
        assertEquals(20L, it.getVerifiedStudents());
        assertEquals(10L, it.getPlacedStudents());
        assertEquals(50.0, it.getPlacementPercentage()); // (10 / 20) * 100
    }

    @Test
    void getCompanyStats_ReturnsCompanyStatsFromRepository() {
        List<CompanyPlacementStat> mockStats = List.of(
                new CompanyPlacementStat("Google", "Technology", 10L, 8L),
                new CompanyPlacementStat("Amazon", "E-Commerce", 6L, 5L)
        );
        when(applicationRepository.findCompanyPlacementStats(ApplicationStatus.SELECTED)).thenReturn(mockStats);

        List<CompanyPlacementStat> result = placementAnalyticsService.getCompanyStats();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Google", result.get(0).getCompanyName());
    }

    @Test
    void getSalaryPackageStats_ParsesJobSalariesAndReturnsComputedStats() {
        Job job1 = new Job();
        job1.setSalaryPackage("15 LPA");

        Job job2 = new Job();
        job2.setSalaryPackage("9 LPA");

        StudentProfile student = new StudentProfile();

        Application app1 = new Application(student, job1);
        app1.setStatus(ApplicationStatus.SELECTED);

        Application app2 = new Application(student, job2);
        app2.setStatus(ApplicationStatus.SELECTED);

        when(applicationRepository.findSelectedApplicationsWithJobDetails(ApplicationStatus.SELECTED))
                .thenReturn(List.of(app1, app2));

        SalaryPackageStats stats = placementAnalyticsService.getSalaryPackageStats();

        assertNotNull(stats);
        assertEquals(15.0, stats.getHighestPackageLpa());
        assertEquals(12.0, stats.getAveragePackageLpa());
        assertEquals(12.0, stats.getMedianPackageLpa());
        assertEquals(2, stats.getTotalAnalyzedOffers());
    }

    @Test
    void getPlacementAnalyticsReport_AssemblesCompositeReport() {
        when(studentProfileRepository.count()).thenReturn(10L);
        when(studentProfileRepository.countByVerificationStatus(VerificationStatus.VERIFIED)).thenReturn(8L);
        when(applicationRepository.countDistinctPlacedStudents(ApplicationStatus.SELECTED)).thenReturn(4L);
        when(applicationRepository.count()).thenReturn(15L);
        when(applicationRepository.countByStatus(ApplicationStatus.SELECTED)).thenReturn(5L);
        when(jobRepository.countByStatus(JobStatus.APPROVED)).thenReturn(3L);
        when(companyRepository.countByApprovalStatus(CompanyApprovalStatus.APPROVED)).thenReturn(2L);
        when(studentProfileRepository.findDepartmentStudentCounts()).thenReturn(List.of());
        when(applicationRepository.findPlacedStudentCountByDepartment(ApplicationStatus.SELECTED)).thenReturn(List.of());
        when(applicationRepository.findCompanyPlacementStats(ApplicationStatus.SELECTED)).thenReturn(List.of());
        when(applicationRepository.findSelectedApplicationsWithJobDetails(ApplicationStatus.SELECTED)).thenReturn(List.of());

        PlacementAnalyticsReport report = placementAnalyticsService.getPlacementAnalyticsReport();

        assertNotNull(report);
        assertNotNull(report.getOverview());
        assertNotNull(report.getDepartmentStats());
        assertNotNull(report.getCompanyStats());
        assertNotNull(report.getSalaryStats());
        assertNotNull(report.getGeneratedAt());
        assertEquals(4L, report.getOverview().getPlacedStudents());
    }

    @Test
    void getPlacedStudentsCount_ReturnsCount() {
        when(applicationRepository.countDistinctPlacedStudents(ApplicationStatus.SELECTED)).thenReturn(42L);

        long count = placementAnalyticsService.getPlacedStudentsCount();

        assertEquals(42L, count);
        verify(applicationRepository).countDistinctPlacedStudents(ApplicationStatus.SELECTED);
    }
}
