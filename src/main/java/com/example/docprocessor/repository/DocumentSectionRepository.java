package com.example.docprocessor.repository;

import com.example.docprocessor.model.DocumentSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {
    List<DocumentSection> findByDocumentId(Long documentId);
}
