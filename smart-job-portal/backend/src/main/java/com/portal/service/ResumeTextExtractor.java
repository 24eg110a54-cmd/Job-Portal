package com.portal.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Pulls plain text out of PDF, DOCX and TXT resumes. */
@Component
public class ResumeTextExtractor {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    /** PDF files always start with the 4-byte magic sequence %PDF. */
    private static final byte[] PDF_MAGIC = { 0x25, 0x50, 0x44, 0x46 }; // %PDF

    /**
     * Extracts plain text from the uploaded resume.
     * Validates both the declared MIME content-type and (for PDFs) the file magic bytes
     * before dispatching to the appropriate parser.
     */
    public String extract(MultipartFile file) throws IOException {
        // --- Content-type validation (C-3) ---
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type '" + contentType + "'. Only PDF, DOCX or TXT resumes are accepted.");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        byte[] bytes = file.getBytes();

        if (contentType.equals("application/pdf") || name.endsWith(".pdf")) {
            // Magic-byte check: a valid PDF must start with %PDF
            if (!hasMagicBytes(bytes, PDF_MAGIC)) {
                throw new IllegalArgumentException("The uploaded file does not appear to be a valid PDF.");
            }
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                return new PDFTextStripper().getText(doc);
            }
        }

        if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || name.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
                 XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                return ex.getText();
            }
        }

        if (contentType.equals("text/plain") || name.endsWith(".txt")) {
            return new String(bytes, StandardCharsets.UTF_8);
        }

        throw new IllegalArgumentException("Only PDF, DOCX or TXT resumes are supported.");
    }

    /** Returns true when the first bytes of {@code data} match {@code magic}. */
    private static boolean hasMagicBytes(byte[] data, byte[] magic) {
        if (data.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) return false;
        }
        return true;
    }
}
