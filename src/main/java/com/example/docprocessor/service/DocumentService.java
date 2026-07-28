package com.example.docprocessor.service;

import com.example.docprocessor.model.Document;
import com.example.docprocessor.model.DocumentSection;
import com.example.docprocessor.repository.DocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public List<Document> searchAndFilterDocuments(String query, String fileType) {
        String cleanQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        String cleanType = (fileType == null || fileType.trim().isEmpty() || fileType.equalsIgnoreCase("ALL")) ? null : fileType.trim();
        return documentRepository.searchAndFilter(cleanQuery, cleanType);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    @Transactional
    public Document processDocument(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "unknown_file";
        }

        String ext = getFileExtension(filename).toUpperCase();
        Document doc = new Document();
        doc.setFilename(filename);
        doc.setFileType(ext);
        doc.setUploadTime(LocalDateTime.now());

        try (InputStream is = file.getInputStream()) {
            switch (ext) {
                case "PDF":
                    parsePdf(doc, is);
                    break;
                case "XML":
                    parseXml(doc, is);
                    break;
                case "DOCX":
                case "DOC":
                    parseWord(doc, is);
                    break;
                case "XLSX":
                case "XLS":
                    parseExcel(doc, is);
                    break;
                case "PPTX":
                case "PPT":
                    parsePowerPoint(doc, is);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + ext);
            }
            doc.setProcessed(true);
            doc.setStatusMessage("Success: Fully processed sections & data extracted.");
        } catch (Exception e) {
            doc.setProcessed(false);
            doc.setStatusMessage("Failed: " + e.getMessage());
        }

        return documentRepository.save(doc);
    }

    private String getFileExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return index == -1 ? "" : filename.substring(index + 1);
    }

    // PDF Parsing using Apache PDFBox
    private void parsePdf(Document doc, InputStream is) throws Exception {
        try (PDDocument pdfDoc = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdfDoc.getNumberOfPages();
            if (totalPages == 0) {
                doc.addSection(new DocumentSection("Page", "Blank Document", "No text found."));
                return;
            }
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(pdfDoc).trim();
                if (text.isEmpty()) {
                    text = "[Empty Page]";
                }
                doc.addSection(new DocumentSection("Page", "Page " + i, text));
            }
        }
    }

    // Word Parsing using Apache POI
    private void parseWord(Document doc, InputStream is) throws Exception {
        try (XWPFDocument wordDoc = new XWPFDocument(is)) {
            List<XWPFParagraph> paragraphs = wordDoc.getParagraphs();
            StringBuilder paragraphBuffer = new StringBuilder();
            int sectionCount = 1;

            for (XWPFParagraph p : paragraphs) {
                String text = p.getText().trim();
                if (!text.isEmpty()) {
                    paragraphBuffer.append(text).append("\n");
                    // Split into sections per 5 paragraphs or headers
                    if (p.getStyleID() != null && p.getStyleID().startsWith("Heading")) {
                        if (paragraphBuffer.length() > 0) {
                            doc.addSection(new DocumentSection("Section", "Heading-Split Section " + sectionCount++, paragraphBuffer.toString().trim()));
                            paragraphBuffer.setLength(0);
                        }
                    }
                }
            }
            if (paragraphBuffer.length() > 0 || sectionCount == 1) {
                doc.addSection(new DocumentSection("Section", "Main Content Section " + sectionCount,
                        paragraphBuffer.length() == 0 ? "No text elements found." : paragraphBuffer.toString().trim()));
            }
        }
    }

    // Excel Parsing using Apache POI
    private void parseExcel(Document doc, InputStream is) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            int totalSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < totalSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                StringBuilder sheetContent = new StringBuilder();
                for (Row row : sheet) {
                    StringBuilder rowContent = new StringBuilder();
                    for (Cell cell : row) {
                        rowContent.append(getCellValueAsString(cell)).append("\t");
                    }
                    if (!rowContent.toString().trim().isEmpty()) {
                        sheetContent.append(rowContent.toString().trim()).append("\n");
                    }
                }
                String content = sheetContent.toString().trim();
                doc.addSection(new DocumentSection("Tab", "Sheet: " + sheet.getSheetName(),
                        content.isEmpty() ? "[Empty Sheet]" : content));
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    // PowerPoint Parsing using Apache POI
    private void parsePowerPoint(Document doc, InputStream is) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
            List<XSLFSlide> slides = ppt.getSlides();
            int slideNum = 1;
            for (XSLFSlide slide : slides) {
                StringBuilder slideText = new StringBuilder();
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape) {
                        slideText.append(((org.apache.poi.xslf.usermodel.XSLFTextShape) shape).getText()).append("\n");
                    }
                });
                String content = slideText.toString().trim();
                doc.addSection(new DocumentSection("Slide", "Slide " + slideNum++,
                        content.isEmpty() ? "[Empty Slide]" : content));
            }
        }
    }

    // XML Parsing using standard DOM builder
    private void parseXml(Document doc, InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document xmlDoc = builder.parse(is);
        xmlDoc.getDocumentElement().normalize();

        NodeList nodeList = xmlDoc.getDocumentElement().getChildNodes();
        int sectionCount = 1;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();
                String content = node.getTextContent().trim();
                if (!content.isEmpty()) {
                    doc.addSection(new DocumentSection("XML Node", "Node <" + nodeName + "> (" + sectionCount++ + ")", content));
                }
            }
        }
        if (doc.getSections().isEmpty()) {
            doc.addSection(new DocumentSection("XML Node", "Root Element <" + xmlDoc.getDocumentElement().getNodeName() + ">", xmlDoc.getDocumentElement().getTextContent().trim()));
        }
    }
}
