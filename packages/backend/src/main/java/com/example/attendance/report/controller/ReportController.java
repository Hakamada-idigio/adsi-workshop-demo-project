package com.example.attendance.report.controller;

import com.example.attendance.report.dto.MonthlyReportResponse;
import com.example.attendance.report.service.CsvExportService;
import com.example.attendance.report.service.PdfExportService;
import com.example.attendance.report.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    public ReportController(
            ReportService reportService,
            CsvExportService csvExportService,
            PdfExportService pdfExportService) {
        this.reportService = reportService;
        this.csvExportService = csvExportService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/monthly")
    public MonthlyReportResponse getMonthlyReport(
            @RequestParam String month,
            @RequestParam(required = false) UUID departmentId) {
        return reportService.getMonthlyReport(month, departmentId);
    }

    @GetMapping("/monthly/csv")
    public ResponseEntity<byte[]> getCsv(
            @RequestParam String month,
            @RequestParam(required = false) UUID departmentId) {
        var report = reportService.getMonthlyReport(month, departmentId);
        var csvBytes = csvExportService.exportToCsv(month, report.records());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance-%s.csv\"".formatted(month))
                .body(csvBytes);
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> getPdf(
            @RequestParam String month,
            @RequestParam(required = false) UUID departmentId) {
        var report = reportService.getMonthlyReport(month, departmentId);
        var pdfBytes = pdfExportService.exportToPdf(month, report.records());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance-%s.pdf\"".formatted(month))
                .body(pdfBytes);
    }
}
