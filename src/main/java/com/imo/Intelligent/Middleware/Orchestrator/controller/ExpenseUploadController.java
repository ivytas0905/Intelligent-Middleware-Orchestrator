package com.imo.Intelligent.Middleware.Orchestrator.controller;


import com.imo.Intelligent.Middleware.Orchestrator.agent.OrchestrationAgent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import java.io.InputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/agent")
public class ExpenseUploadController {

    private final OrchestrationAgent agent;

    public ExpenseUploadController(OrchestrationAgent agent) {
        this.agent = agent;
    }

    @PostMapping(value = "/audit/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String auditFromPdf(@RequestParam("file") MultipartFile file) throws IOException {

        // 校验文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return "Error: Only PDF files are accepted.";
        }

        // 校验文件大小（最大 10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return "Error: File size exceeds 10MB limit.";
        }

        // 提取 PDF 文本
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String receiptText = stripper.getText(document);

            if (receiptText == null || receiptText.isBlank()) {
                return "Error: Could not extract text from PDF. Please ensure the PDF is not scanned image.";
            }

            // 交给 Agent 审计
            return agent.auditExpenseReport(receiptText);
        }
    }
}
