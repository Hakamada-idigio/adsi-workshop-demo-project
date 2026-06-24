package com.example.attendance.report.service;

import com.example.attendance.report.dto.EmployeeMonthlyRecord;

import java.util.List;

public interface PdfExportService {

    byte[] exportToPdf(String month, List<EmployeeMonthlyRecord> records);
}
