package com.example.docprocessor.controller;

import com.example.docprocessor.model.Document;
import com.example.docprocessor.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Controller", description = "REST APIs for processing and exploring documents")
public class DocumentApiController {

    private final DocumentService documentService;

    public DocumentApiController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Upload and process any document (PDF, Word, Excel, PowerPoint, XML)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(
            @Parameter(description = "The file to upload") @RequestParam("file") MultipartFile file) {
        Document doc = documentService.processDocument(file);
        return ResponseEntity.ok(doc);
    }

    @Operation(summary = "Get list of all processed documents with optional search & filter")
    @GetMapping
    public ResponseEntity<List<Document>> listDocuments(
            @Parameter(description = "Search query to match in filename or section contents") @RequestParam(required = false) String query,
            @Parameter(description = "File type filter (e.g. PDF, XML, DOCX, XLSX, PPTX)") @RequestParam(required = false) String fileType) {
        List<Document> docs = documentService.searchAndFilterDocuments(query, fileType);
        return ResponseEntity.ok(docs);
    }

    @Operation(summary = "Retrieve a document with all its separated sections, tabs or slides")
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentDetails(
            @Parameter(description = "ID of the processed document") @PathVariable Long id) {
        Document doc = documentService.getDocumentById(id);
        // Eager-loading simulation or direct entity mapping
        doc.getSections().size(); // force load
        return ResponseEntity.ok(doc);
    }
}
