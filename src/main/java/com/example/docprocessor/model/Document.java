package com.example.docprocessor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String fileType; // PDF, XML, DOCX, XLSX, PPTX, etc.
    private boolean processed;
    private LocalDateTime uploadTime;
    private String statusMessage;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentSection> sections = new ArrayList<>();

    public Document() {}

    public Document(String filename, String fileType, boolean processed, LocalDateTime uploadTime, String statusMessage) {
        this.filename = filename;
        this.fileType = fileType;
        this.processed = processed;
        this.uploadTime = uploadTime;
        this.statusMessage = statusMessage;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public List<DocumentSection> getSections() { return sections; }
    public void setSections(List<DocumentSection> sections) { this.sections = sections; }

    public void addSection(DocumentSection section) {
        sections.add(section);
        section.setDocument(this);
    }
}
