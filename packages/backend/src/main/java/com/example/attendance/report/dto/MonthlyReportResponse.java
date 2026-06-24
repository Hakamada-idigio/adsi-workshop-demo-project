package com.example.attendance.report.dto;

import java.util.List;

public record MonthlyReportResponse(
    String month,
    List<EmployeeMonthlyRecord> records
) {}
