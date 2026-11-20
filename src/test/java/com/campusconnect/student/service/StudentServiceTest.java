package com.campusconnect.student.service;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.common.storage.FileStorageService;
import com.campusconnect.student.dto.StudentProfileUpdateRequest;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StudentService studentService;

    private User studentUser;
    private StudentProfile studentProfile;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(1L);
        studentUser.setFullName("John Doe");
        studentUser.setEmail("john.doe@college.edu");
        studentUser.setRole(Role.STUDENT);

        studentProfile = new StudentProfile();
        studentProfile.setId(10L);
        studentProfile.setUser(studentUser);
        studentProfile.setRollNumber("2022CS101");
        studentProfile.setDepartment("Computer Science");
        studentProfile.setGraduationYear(2026);
        studentProfile.setCgpa(new BigDecimal("8.50"));
        studentProfile.setActiveBacklogs(0);
        studentProfile.setPhoneNumber("+91 9876543210");
        studentProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        studentProfile.setVerifiedBy("officer@campusconnect.edu");
        studentProfile.setVerifiedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getProfileByEmail_Found_ReturnsProfile() {
        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));

        StudentProfile found = studentService.getProfileByEmail("john.doe@college.edu");

        assertNotNull(found);
        assertEquals("2022CS101", found.getRollNumber());
    }

    @Test
    void getProfileByEmail_NotFound_ThrowsException() {
        when(studentProfileRepository.findByUserEmail("unknown@college.edu"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                studentService.getProfileByEmail("unknown@college.edu")
        );
    }

    @Test
    void updateProfile_PhoneOnlyChange_RetainsVerifiedStatus() {
        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));

        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setFullName("John Updated Doe");
        request.setPhoneNumber("+91 9999999999");
        request.setDepartment("Computer Science");
        request.setGraduationYear(2026);
        request.setCgpa(new BigDecimal("8.50"));
        request.setActiveBacklogs(0);

        boolean reVerificationTriggered = studentService.updateProfile("john.doe@college.edu", request);

        assertFalse(reVerificationTriggered);
        assertEquals(VerificationStatus.VERIFIED, studentProfile.getVerificationStatus());
        assertNotNull(studentProfile.getVerifiedAt());
        assertNotNull(studentProfile.getVerifiedBy());
        assertEquals("+91 9999999999", studentProfile.getPhoneNumber());
        assertEquals("John Updated Doe", studentUser.getFullName());
        verify(userRepository).save(studentUser);
        verify(studentProfileRepository).save(studentProfile);
    }

    @Test
    void updateProfile_CgpaChange_ResetsToPendingStatus() {
        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));

        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setFullName("John Doe");
        request.setPhoneNumber("+91 9876543210");
        request.setDepartment("Computer Science");
        request.setGraduationYear(2026);
        request.setCgpa(new BigDecimal("8.95")); // CGPA changed!
        request.setActiveBacklogs(0);

        boolean reVerificationTriggered = studentService.updateProfile("john.doe@college.edu", request);

        assertTrue(reVerificationTriggered);
        assertEquals(VerificationStatus.PENDING, studentProfile.getVerificationStatus());
        assertNull(studentProfile.getVerifiedAt());
        assertNull(studentProfile.getVerifiedBy());
        assertNull(studentProfile.getRejectionReason());
        assertEquals(new BigDecimal("8.95"), studentProfile.getCgpa());
        verify(studentProfileRepository).save(studentProfile);
    }

    @Test
    void updateProfile_BacklogsChange_ResetsToPendingStatus() {
        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));

        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setFullName("John Doe");
        request.setPhoneNumber("+91 9876543210");
        request.setDepartment("Computer Science");
        request.setGraduationYear(2026);
        request.setCgpa(new BigDecimal("8.50"));
        request.setActiveBacklogs(1); // Backlog added!

        boolean reVerificationTriggered = studentService.updateProfile("john.doe@college.edu", request);

        assertTrue(reVerificationTriggered);
        assertEquals(VerificationStatus.PENDING, studentProfile.getVerificationStatus());
        assertEquals(1, studentProfile.getActiveBacklogs());
    }

    @Test
    void uploadResume_FirstUpload_SavesFileNameAndTimestamp() {
        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));
        when(fileStorageService.storeFile(any(), eq(10L)))
                .thenReturn("student_10_uuid.pdf");

        MockMultipartFile file = new MockMultipartFile("resume", "my_resume.pdf", "application/pdf", "Content".getBytes());

        studentService.uploadResume("john.doe@college.edu", file);

        assertEquals("student_10_uuid.pdf", studentProfile.getResumeFileName());
        assertEquals("my_resume.pdf", studentProfile.getResumeOriginalName());
        assertNotNull(studentProfile.getResumeUploadedAt());
        verify(studentProfileRepository).save(studentProfile);
    }

    @Test
    void uploadResume_ReplacesExistingResume_DeletesOldFile() {
        studentProfile.setResumeFileName("old_resume.pdf");
        studentProfile.setResumeOriginalName("old_cv.pdf");

        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));
        when(fileStorageService.storeFile(any(), eq(10L)))
                .thenReturn("student_10_new_uuid.pdf");

        MockMultipartFile file = new MockMultipartFile("resume", "updated_resume.pdf", "application/pdf", "New Content".getBytes());

        studentService.uploadResume("john.doe@college.edu", file);

        verify(fileStorageService).deleteFile("old_resume.pdf");
        assertEquals("student_10_new_uuid.pdf", studentProfile.getResumeFileName());
        assertEquals("updated_resume.pdf", studentProfile.getResumeOriginalName());
    }

    @Test
    void getResumeResource_NoResume_ThrowsException() {
        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));

        assertThrows(IllegalArgumentException.class, () ->
                studentService.getResumeResource("john.doe@college.edu")
        );
    }

    @Test
    void getResumeResource_HasResume_ReturnsResource() {
        studentProfile.setResumeFileName("stored.pdf");
        studentProfile.setResumeOriginalName("original.pdf");

        when(studentProfileRepository.findByUserEmail("john.doe@college.edu"))
                .thenReturn(Optional.of(studentProfile));
        Resource dummyResource = new ByteArrayResource("pdf-bytes".getBytes());
        when(fileStorageService.loadFileAsResource("stored.pdf")).thenReturn(dummyResource);

        StudentService.ResumeDownload download = studentService.getResumeResource("john.doe@college.edu");

        assertNotNull(download);
        assertEquals("original.pdf", download.originalFilename());
        assertEquals(dummyResource, download.resource());
    }
}
