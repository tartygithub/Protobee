# Functional Specification

This document details the functional specifications and architectural requirements for the **Corporate Document Processor Web Application**.

## 1. Architectural Highlights
- **Core Platform:** Spring Boot 3.4.1 / Java 21.
- **Build Engine:** Apache Maven.
- **Database Architecture:** Built on top of Spring Data JPA and Hibernate. Standard H2 In-Memory DB is configured as default, but full drivers and configuration properties templates are built-in for zero-effort transition to:
  - **Oracle Database**
  - **Microsoft SQL Server**
  - **PostgreSQL**
- **User Interface:** Responsive Tailwind CSS styled frontend utilizing Thymeleaf Templating.

## 2. Authentication and User Management (LDAP & DB)
- **Basic DB Authentication:** BCrypt hashed password store inside DB `users` table.
- **LDAP Integration:** Configurable switch (`app.auth.ldapEnabled=true`) in `application.properties` that mounts LDAP servers (such as Active Directory or OpenLDAP) as the active user registry.
- **Account Protection:** Lockout mechanism automatically triggered after 5 consecutive failed login attempts.
- **Expiration Mechanism:** Custom `passwordExpirationDate` is tracked. Spring Security automatically handles credentials expired states to prompt administration reset.
- **Full Maintenance Matrix:** Admins can:
  - Create Users (self-registration or provisioning).
  - Delete Users.
  - Force Lock or Unlock users.
  - Reset Passwords.
  - Force immediate password expiration.

## 3. Advanced Document Parser System
The ingestion service extracts text structures and decomposes documents based on format-native semantics:
- **XML:** Decouples nodes dynamically and registers child node properties.
- **Word (.docx/.doc):** Processes headings and text paragraphs, creating heading-delimited sections.
- **Excel (.xlsx/.xls):** Parses workbook sheets, converting cellular rows and tables into sheet-delimited tabs.
- **PowerPoint (.pptx/.ppt):** Reads all slide indexes and extracts shape elements to populate slides.
- **PDF:** Leverages Apache PDFBox to strip text page-by-page.

## 4. Search, Catalog and Inspect
- All extracted segments are stored in the database as `DocumentSection` entities linked to a parent `Document`.
- Full-text search searches across both parent document metadata (filenames) and children node content (individual text elements inside slides or sheets).
- Filters filter results dynamically by format (PDF, Word, XML, Excel, PPTX).
