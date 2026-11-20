package com.campusconnect.student.service;

import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.common.storage.FileStorageService;
import com.campusconnect.student.dto.StudentProfileUpdateRequest;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public StudentService(StudentProfileRepository studentProfileRepository,
                          UserRepository userRepository,
                          FileStorageService fileStorageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public StudentProfile getProfileByEmail(String email) {
        return studentProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for email: " + email));
    }

    @Transactional
    public boolean updateProfile(String email, StudentProfileUpdateRequest request) {
        StudentProfile profile = getProfileByEmail(email);
        User user = profile.getUser();

        user.setFullName(request.getFullName().trim());
        userRepository.save(user);

        boolean academicChanged = !profile.getDepartment().equalsIgnoreCase(request.getDepartment().trim()) ||
                !profile.getGraduationYear().equals(request.getGraduationYear()) ||
                profile.getCgpa().compareTo(request.getCgpa()) != 0 ||
                !profile.getActiveBacklogs().equals(request.getActiveBacklogs());

        profile.setDepartment(request.getDepartment().trim());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setCgpa(request.getCgpa());
        profile.setActiveBacklogs(request.getActiveBacklogs());
        profile.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);

        if (academicChanged) {
            profile.setVerificationStatus(VerificationStatus.PENDING);
            profile.setRejectionReason(null);
            profile.setVerifiedAt(null);
            profile.setVerifiedBy(null);
        }

        studentProfileRepository.save(profile);
        return academicChanged;
    }

    @Transactional
    public void uploadResume(String email, MultipartFile file) {
        StudentProfile profile = getProfileByEmail(email);

        if (profile.hasResume()) {
            fileStorageService.deleteFile(profile.getResumeFileName());
        }

        String storedFileName = fileStorageService.storeFile(file, profile.getId());
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";

        profile.setResumeFileName(storedFileName);
        profile.setResumeOriginalName(originalName);
        profile.setResumeUploadedAt(LocalDateTime.now());

        studentProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ResumeDownload getResumeResource(String email) {
        StudentProfile profile = getProfileByEmail(email);
        if (!profile.hasResume()) {
            throw new IllegalArgumentException("No resume has been uploaded yet.");
        }

        Resource resource = fileStorageService.loadFileAsResource(profile.getResumeFileName());
        return new ResumeDownload(resource, profile.getResumeOriginalName());
    }

    public record ResumeDownload(Resource resource, String originalFilename) {}
}
