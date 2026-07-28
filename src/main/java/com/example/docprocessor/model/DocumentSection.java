package com.example.docprocessor.model;

import javax.persistence.*;

@Entity
@Table(name = "document_sections")
public class DocumentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private String sectionType; // "Tab", "Slide", "Page", "Element"
    private String sectionName; // e.g., "Sheet 1", "Slide 3", "Page 2"

    @Column(columnDefinition = "TEXT")
    private String content;

    public DocumentSection() {}

    public DocumentSection(String sectionType, String sectionName, String content) {
        this.sectionType = sectionType;
        this.sectionName = sectionName;
        this.content = content;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getSectionType() { return sectionType; }
    public void setSectionType(String sectionType) { this.sectionType = sectionType; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
