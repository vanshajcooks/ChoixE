package com.campusconnect.auth.service;

import com.campusconnect.auth.dto.RecruiterRegisterRequest;
import com.campusconnect.auth.dto.StudentRegisterRequest;
import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.entity.UserStatus;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerStudent(StudentRegisterRequest request) {
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with email " + request.getEmail() + " already exists");
        }
        if (studentProfileRepository.existsByRollNumber(request.getRollNumber().trim())) {
            throw new IllegalArgumentException("A student profile with roll number " + request.getRollNumber() + " already exists");
        }

        User user = new User(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        User savedUser = userRepository.save(user);

        StudentProfile profile = new StudentProfile(
                savedUser,
                request.getRollNumber().trim(),
                request.getDepartment().trim(),
                request.getGraduationYear(),
                request.getCgpa(),
                request.getActiveBacklogs()
        );
        profile.setVerificationStatus(VerificationStatus.PENDING);
        studentProfileRepository.save(profile);

        return savedUser;
    }

    @Transactional
    public User registerRecruiter(RecruiterRegisterRequest request) {
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with email " + request.getEmail() + " already exists");
        }

        User user = new User(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                Role.RECRUITER,
                UserStatus.ACTIVE
        );
        User savedUser = userRepository.save(user);

        Company company = new Company(
                savedUser,
                request.getCompanyName().trim(),
                request.getIndustry().trim(),
                request.getWebsite() != null ? request.getWebsite().trim() : null,
                request.getDescription() != null ? request.getDescription().trim() : null
        );
        company.setApprovalStatus(CompanyApprovalStatus.PENDING);
        companyRepository.save(company);

        return savedUser;
    }
}
