package com.campusconnect.officer.service;

import com.campusconnect.common.storage.FileStorageService;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import com.campusconnect.student.service.StudentService.ResumeDownload;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OfficerStudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final FileStorageService fileStorageService;

    public OfficerStudentService(StudentProfileRepository studentProfileRepository,
                                 FileStorageService fileStorageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<StudentProfile> getStudents(VerificationStatus status, String search, Pageable pageable) {
        return studentProfileRepository.searchStudents(status, search, pageable);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return studentProfileRepository.countByVerificationStatus(VerificationStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public StudentProfile getStudentById(Long id) {
        return studentProfileRepository.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found with ID: " + id));
    }

    @Transactional
    public void verifyStudent(Long id, String officerEmail) {
        StudentProfile student = getStudentById(id);
        student.setVerificationStatus(VerificationStatus.VERIFIED);
        student.setVerifiedAt(LocalDateTime.now());
        student.setVerifiedBy(officerEmail);
        student.setRejectionReason(null);
        studentProfileRepository.save(student);
    }

    @Transactional
    public void rejectStudent(Long id, String reason, String officerEmail) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason must be provided.");
        }
        StudentProfile student = getStudentById(id);
        student.setVerificationStatus(VerificationStatus.REJECTED);
        student.setRejectionReason(reason.trim());
        student.setVerifiedAt(LocalDateTime.now());
        student.setVerifiedBy(officerEmail);
        studentProfileRepository.save(student);
    }

    @Transactional(readOnly = true)
    public ResumeDownload getStudentResumeResource(Long studentId) {
        StudentProfile student = getStudentById(studentId);
        if (!student.hasResume()) {
            throw new IllegalArgumentException("This student has not uploaded a resume.");
        }

        Resource resource = fileStorageService.loadFileAsResource(student.getResumeFileName());
        return new ResumeDownload(resource, student.getResumeOriginalName());
    }
}
