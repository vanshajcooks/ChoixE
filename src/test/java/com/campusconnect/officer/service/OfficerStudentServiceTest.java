package com.campusconnect.officer.service;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.common.storage.FileStorageService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import com.campusconnect.student.service.StudentService.ResumeDownload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerStudentServiceTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private OfficerStudentService officerStudentService;

    private StudentProfile studentProfile;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(5L);
        user.setFullName("Jane Smith");
        user.setEmail("jane.smith@college.edu");
        user.setRole(Role.STUDENT);

        studentProfile = new StudentProfile();
        studentProfile.setId(50L);
        studentProfile.setUser(user);
        studentProfile.setRollNumber("2022CS50");
        studentProfile.setDepartment("Computer Science");
        studentProfile.setGraduationYear(2026);
        studentProfile.setCgpa(new BigDecimal("9.10"));
        studentProfile.setActiveBacklogs(0);
        studentProfile.setVerificationStatus(VerificationStatus.PENDING);
    }

    @Test
    void getStudents_CallsRepositorySearch() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<StudentProfile> page = new PageImpl<>(List.of(studentProfile));
        when(studentProfileRepository.searchStudents(VerificationStatus.PENDING, "Jane", pageable))
                .thenReturn(page);

        Page<StudentProfile> result = officerStudentService.getStudents(VerificationStatus.PENDING, "Jane", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getPendingCount_ReturnsRepositoryCount() {
        when(studentProfileRepository.countByVerificationStatus(VerificationStatus.PENDING)).thenReturn(4L);

        long count = officerStudentService.getPendingCount();

        assertEquals(4L, count);
    }

    @Test
    void verifyStudent_ValidProfile_SetsVerifiedAndOfficerMetadata() {
        when(studentProfileRepository.findByIdWithUser(50L)).thenReturn(Optional.of(studentProfile));

        officerStudentService.verifyStudent(50L, "officer@campusconnect.edu");

        assertEquals(VerificationStatus.VERIFIED, studentProfile.getVerificationStatus());
        assertEquals("officer@campusconnect.edu", studentProfile.getVerifiedBy());
        assertNotNull(studentProfile.getVerifiedAt());
        assertNull(studentProfile.getRejectionReason());
        verify(studentProfileRepository).save(studentProfile);
    }

    @Test
    void rejectStudent_ValidReason_SetsRejectedAndReason() {
        when(studentProfileRepository.findByIdWithUser(50L)).thenReturn(Optional.of(studentProfile));

        officerStudentService.rejectStudent(50L, "Marksheet CGPA mismatch. Provide correct transcript.", "officer@campusconnect.edu");

        assertEquals(VerificationStatus.REJECTED, studentProfile.getVerificationStatus());
        assertEquals("Marksheet CGPA mismatch. Provide correct transcript.", studentProfile.getRejectionReason());
        assertEquals("officer@campusconnect.edu", studentProfile.getVerifiedBy());
        assertNotNull(studentProfile.getVerifiedAt());
        verify(studentProfileRepository).save(studentProfile);
    }

    @Test
    void rejectStudent_BlankReason_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                officerStudentService.rejectStudent(50L, "   ", "officer@campusconnect.edu")
        );

        assertEquals("A rejection reason must be provided.", ex.getMessage());
    }

    @Test
    void getStudentResumeResource_NoResume_ThrowsException() {
        when(studentProfileRepository.findByIdWithUser(50L)).thenReturn(Optional.of(studentProfile));

        assertThrows(IllegalArgumentException.class, () ->
                officerStudentService.getStudentResumeResource(50L)
        );
    }

    @Test
    void getStudentResumeResource_HasResume_ReturnsResource() {
        studentProfile.setResumeFileName("file.pdf");
        studentProfile.setResumeOriginalName("resume.pdf");

        when(studentProfileRepository.findByIdWithUser(50L)).thenReturn(Optional.of(studentProfile));
        Resource dummy = new ByteArrayResource("pdf-data".getBytes());
        when(fileStorageService.loadFileAsResource("file.pdf")).thenReturn(dummy);

        ResumeDownload download = officerStudentService.getStudentResumeResource(50L);

        assertNotNull(download);
        assertEquals("resume.pdf", download.originalFilename());
        assertEquals(dummy, download.resource());
    }
}
