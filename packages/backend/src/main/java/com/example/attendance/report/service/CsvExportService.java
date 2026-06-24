package com.example.attendance.report.service;

import com.example.attendance.report.dto.EmployeeMonthlyRecord;

import java.util.List;

public interface CsvExportService {

    byte[] exportToCsv(String month, List<EmployeeMonthlyRecord> records);
}
