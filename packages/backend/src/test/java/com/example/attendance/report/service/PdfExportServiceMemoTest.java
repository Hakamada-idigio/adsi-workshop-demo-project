package com.example.attendance.report.service;

import com.example.attendance.report.dto.EmployeeMonthlyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PDF エクスポート — 備考列")
class PdfExportServiceMemoTest {

    private PdfExportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PdfExportServiceImpl();
    }

    @Test
    @DisplayName("備考フィールドを含むレコードで PDF が生成される")
    void exportToPdf_withRemarks_generatesPdf() {
        // Arrange
        var records = List.of(
                new EmployeeMonthlyRecord(
                        UUID.randomUUID(), "田中太郎", "開発部",
                        20, 9600, 480, 3, "直行3回、直帰2回")
        );

        // Act
        var bytes = service.exportToPdf("2025-01", records);

        // Assert
        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes)).contains("直行3回");
    }

    @Test
    @DisplayName("備考が null でも PDF が正常に生成される")
    void exportToPdf_withNullRemarks_generatesPdf() {
        // Arrange
        var records = List.of(
                new EmployeeMonthlyRecord(
                        UUID.randomUUID(), "鈴木花子", "営業部",
                        18, 8640, 120, 5, null)
        );

        // Act
        var bytes = service.exportToPdf("2025-01", records);

        // Assert
        assertThat(bytes).isNotEmpty();
        assertThat(bytes[0]).isEqualTo((byte) '%');
        assertThat(bytes[1]).isEqualTo((byte) 'P');
    }
}
