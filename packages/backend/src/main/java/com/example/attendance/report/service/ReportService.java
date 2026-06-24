package com.example.attendance.report.service;

import com.example.attendance.report.dto.MonthlyReportResponse;

import java.util.UUID;

public interface ReportService {

    MonthlyReportResponse getMonthlyReport(String month, UUID departmentId);
}
