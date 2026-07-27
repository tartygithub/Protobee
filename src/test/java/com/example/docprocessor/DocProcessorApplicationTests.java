package com.example.docprocessor;

import com.example.docprocessor.model.Document;
import com.example.docprocessor.model.User;
import com.example.docprocessor.service.DocumentService;
import com.example.docprocessor.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DocProcessorApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testUserRegistrationAndLifecycle() {
        // Register user
        User user = userService.registerUser("testuser", "securepass");
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertTrue(passwordEncoder.matches("securepass", user.getPassword()));

        // Check if exists
        Optional<User> found = userService.findByUsername("testuser");
        assertTrue(found.isPresent());

        // Lock user
        userService.lockUser(user.getId());
        assertTrue(userService.findByUsername("testuser").get().isLocked());

        // Unlock user
        userService.unlockUser(user.getId());
        assertFalse(userService.findByUsername("testuser").get().isLocked());

        // Reset password
        userService.resetPassword(user.getId(), "newpass");
        assertTrue(passwordEncoder.matches("newpass", userService.findByUsername("testuser").get().getPassword()));

        // Delete user
        userService.deleteUser(user.getId());
        assertTrue(userService.findByUsername("testuser").isEmpty());
    }

    @Test
    void testXMLDocumentProcessing() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<document>\n" +
                "  <title>Corporate Policy</title>\n" +
                "  <body_text>Content for corporate guidelines.</body_text>\n" +
                "</document>";

        MockMultipartFile file = new MockMultipartFile(
                "file", "policy.xml", "text/xml", xmlContent.getBytes(StandardCharsets.UTF_8));

        Document doc = documentService.processDocument(file);
        assertTrue(doc.isProcessed());
        assertEquals("XML", doc.getFileType());
        assertEquals("policy.xml", doc.getFilename());
        assertFalse(doc.getSections().isEmpty());

        // Search
        List<Document> searchResults = documentService.searchAndFilterDocuments("guidelines", "XML");
        assertFalse(searchResults.isEmpty());
        assertEquals("policy.xml", searchResults.get(0).getFilename());
    }

    @Test
    void testUnsupportedFormatProcessing() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "unsupported.txt", "text/plain", "Simple text content".getBytes(StandardCharsets.UTF_8));

        Document doc = documentService.processDocument(file);
        assertFalse(doc.isProcessed());
        assertTrue(doc.getStatusMessage().contains("Unsupported file type"));
    }
}
