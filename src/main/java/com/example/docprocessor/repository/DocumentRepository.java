package com.example.docprocessor.repository;

import com.example.docprocessor.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.sections s " +
           "WHERE (:query IS NULL OR LOWER(d.filename) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fileType IS NULL OR d.fileType = :fileType)")
    List<Document> searchAndFilter(@Param("query") String query, @Param("fileType") String fileType);
}
