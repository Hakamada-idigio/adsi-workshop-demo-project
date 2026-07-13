package com.example.attendance.report.dto;

import java.util.UUID;

public record EmployeeMonthlyRecord(
    UUID employeeId,
    String employeeName,
    String departmentName,
    int workDays,
    int totalWorkMinutes,
    int totalOvertimeMinutes,
    int absentDays,
    String remarks
) {}
