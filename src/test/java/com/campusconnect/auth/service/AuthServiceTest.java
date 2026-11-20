package com.campusconnect.auth.service;

import com.campusconnect.auth.dto.RecruiterRegisterRequest;
import com.campusconnect.auth.dto.StudentRegisterRequest;
import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.entity.UserStatus;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, studentProfileRepository, companyRepository, passwordEncoder);
    }

    @Test
    void registerStudent_Success() {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@college.edu");
        request.setPassword("SecurePass123");
        request.setConfirmPassword("SecurePass123");
        request.setRollNumber("2022CS1001");
        request.setDepartment("Computer Science & Engineering");
        request.setGraduationYear(2026);
        request.setCgpa(new BigDecimal("8.75"));
        request.setActiveBacklogs(0);

        when(userRepository.existsByEmail("john.doe@college.edu")).thenReturn(false);
        when(studentProfileRepository.existsByRollNumber("2022CS1001")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = authService.registerStudent(request);

        assertNotNull(registeredUser);
        assertEquals("john.doe@college.edu", registeredUser.getEmail());
        assertEquals("hashedPassword", registeredUser.getPassword());
        assertEquals(Role.STUDENT, registeredUser.getRole());
        assertEquals(UserStatus.ACTIVE, registeredUser.getStatus());

        ArgumentCaptor<StudentProfile> profileCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(studentProfileRepository).save(profileCaptor.capture());
        StudentProfile savedProfile = profileCaptor.getValue();
        assertEquals("2022CS1001", savedProfile.getRollNumber());
        assertEquals(VerificationStatus.PENDING, savedProfile.getVerificationStatus());
        assertEquals(new BigDecimal("8.75"), savedProfile.getCgpa());
    }

    @Test
    void registerStudent_PasswordMismatch_ThrowsException() {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setEmail("john@college.edu");
        request.setPassword("Password123");
        request.setConfirmPassword("MismatchPass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerStudent(request));

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerStudent_DuplicateEmail_ThrowsException() {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setEmail("john@college.edu");
        request.setPassword("Password123");
        request.setConfirmPassword("Password123");

        when(userRepository.existsByEmail("john@college.edu")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerStudent(request));

        assertEquals("An account with email john@college.edu already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerStudent_DuplicateRollNumber_ThrowsException() {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setEmail("new@college.edu");
        request.setPassword("Password123");
        request.setConfirmPassword("Password123");
        request.setRollNumber("EXISTING_ROLL");

        when(userRepository.existsByEmail("new@college.edu")).thenReturn(false);
        when(studentProfileRepository.existsByRollNumber("EXISTING_ROLL")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerStudent(request));

        assertEquals("A student profile with roll number EXISTING_ROLL already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRecruiter_Success() {
        RecruiterRegisterRequest request = new RecruiterRegisterRequest();
        request.setFullName("Jane Smith");
        request.setEmail("jane@techcorp.com");
        request.setPassword("SecurePass123");
        request.setConfirmPassword("SecurePass123");
        request.setCompanyName("TechCorp Global");
        request.setIndustry("Information Technology");
        request.setWebsite("https://techcorp.com");
        request.setDescription("Leading software provider.");

        when(userRepository.existsByEmail("jane@techcorp.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = authService.registerRecruiter(request);

        assertNotNull(registeredUser);
        assertEquals("jane@techcorp.com", registeredUser.getEmail());
        assertEquals(Role.RECRUITER, registeredUser.getRole());

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        Company savedCompany = companyCaptor.getValue();
        assertEquals("TechCorp Global", savedCompany.getCompanyName());
    }
}
