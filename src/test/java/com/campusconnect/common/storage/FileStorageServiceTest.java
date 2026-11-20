package com.campusconnect.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void storeFile_ValidPdf_SavesSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "sample_resume.pdf",
                "application/pdf",
                "Dummy PDF content".getBytes()
        );

        String storedFileName = fileStorageService.storeFile(file, 101L);

        assertNotNull(storedFileName);
        assertTrue(storedFileName.startsWith("student_101_"));
        assertTrue(storedFileName.endsWith(".pdf"));

        Resource resource = fileStorageService.loadFileAsResource(storedFileName);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void storeFile_ValidDocx_SavesSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "my_cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Dummy DOCX content".getBytes()
        );

        String storedFileName = fileStorageService.storeFile(file, 202L);

        assertNotNull(storedFileName);
        assertTrue(storedFileName.startsWith("student_202_"));
        assertTrue(storedFileName.endsWith(".docx"));
    }

    @Test
    void storeFile_InvalidExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "malicious.exe",
                "application/octet-stream",
                "exe content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeFile(file, 101L)
        );
        assertTrue(ex.getMessage().contains("Allowed formats: PDF, DOC, DOCX"));
    }

    @Test
    void storeFile_Exceeds5Mb_ThrowsException() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "huge.pdf",
                "application/pdf",
                largeBytes
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeFile(file, 101L)
        );
        assertTrue(ex.getMessage().contains("exceeds maximum allowed limit of 5MB"));
    }

    @Test
    void storeFile_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeFile(file, 101L)
        );
        assertTrue(ex.getMessage().contains("Cannot store empty file"));
    }

    @Test
    void deleteFile_RemovesFile() {
        MockMultipartFile file = new MockMultipartFile(
                "resume",
                "to_delete.pdf",
                "application/pdf",
                "temporary content".getBytes()
        );

        String storedFileName = fileStorageService.storeFile(file, 303L);
        fileStorageService.deleteFile(storedFileName);

        assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.loadFileAsResource(storedFileName)
        );
    }
}
