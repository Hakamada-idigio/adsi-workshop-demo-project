package com.example.attendance.report.service;

import com.example.attendance.report.dto.EmployeeMonthlyRecord;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PdfExportServiceImpl implements PdfExportService {

    @Override
    public byte[] exportToPdf(String month, List<EmployeeMonthlyRecord> records) {
        try {
            InputStream template = getClass().getResourceAsStream(
                    "/reports/monthly-report.jrxml");
            if (template == null) {
                throw new IllegalStateException("JasperReports template not found");
            }

            var jasperReport = JasperCompileManager.compileReport(template);
            var params = new HashMap<String, Object>();
            params.put("month", month);

            var mapRecords = records.stream()
                    .<Map<String, ?>>map(PdfExportServiceImpl::toMap)
                    .toList();
            var dataSource = new JRMapCollectionDataSource(mapRecords);
            var jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, dataSource);

            var pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            log.info("PDF exported: month={}, records={}", month, records.size());
            return pdfBytes;
        } catch (JRException e) {
            log.error("Failed to generate PDF report", e);
            throw new IllegalStateException("Failed to generate PDF report", e);
        }
    }

    private static Map<String, Object> toMap(EmployeeMonthlyRecord record) {
        var map = new HashMap<String, Object>();
        map.put("employeeName", record.employeeName());
        map.put("departmentName", record.departmentName());
        map.put("workDays", record.workDays());
        map.put("totalWorkMinutes", record.totalWorkMinutes());
        map.put("totalOvertimeMinutes", record.totalOvertimeMinutes());
        map.put("absentDays", record.absentDays());
        map.put("remarks", record.remarks() != null ? record.remarks() : "");
        return map;
    }
}
