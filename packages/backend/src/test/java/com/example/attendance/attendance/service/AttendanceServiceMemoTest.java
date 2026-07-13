package com.example.attendance.attendance.service;

import com.example.attendance.attendance.entity.AttendanceRecord;
import com.example.attendance.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.department.entity.Department;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("打刻メモ機能")
class AttendanceServiceMemoTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-15T00:00:00Z");
    private static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");
    private static final LocalDate TODAY_TOKYO = LocalDate.of(2025, 1, 15);

    @Mock
    private AttendanceRecordRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AttendanceServiceImpl service;

    private Employee employee;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(FIXED_INSTANT, ZONE_TOKYO);
        service = new AttendanceServiceImpl(attendanceRepository, employeeRepository, clock);

        var department = Department.builder()
                .id(UUID.randomUUID())
                .name("Engineering")
                .build();

        employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed")
                .department(department)
                .role(Role.EMPLOYEE)
                .isManager(false)
                .hireDate(LocalDate.of(2024, 4, 1))
                .build();
    }

    @Nested
    @DisplayName("出勤打刻にメモを付与")
    class ClockInWithMemo {

        @Test
        @DisplayName("メモ付きで出勤打刻するとレスポンスにメモが含まれる")
        void clockIn_withMemo_returnsRecordWithMemo() {
            // Arrange
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(attendanceRepository.findByEmployeeIdAndWorkDateAndClockOutIsNull(employee.getId(), TODAY_TOKYO))
                    .thenReturn(Optional.empty());
            when(attendanceRepository.save(any(AttendanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.clockIn(employee.getId(), "直行");

            // Assert
            assertThat(result.clockInMemo()).isEqualTo("直行");
        }

        @Test
        @DisplayName("メモなし（null）で出勤打刻できる")
        void clockIn_withoutMemo_returnsRecordWithNullMemo() {
            // Arrange
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(attendanceRepository.findByEmployeeIdAndWorkDateAndClockOutIsNull(employee.getId(), TODAY_TOKYO))
                    .thenReturn(Optional.empty());
            when(attendanceRepository.save(any(AttendanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.clockIn(employee.getId(), null);

            // Assert
            assertThat(result.clockInMemo()).isNull();
        }

        @Test
        @DisplayName("メモが100文字を超える場合はバリデーションエラー")
        void clockIn_memoTooLong_throwsBadRequest() {
            // Arrange
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            var longMemo = "あ".repeat(101);

            // Act & Assert
            assertThatThrownBy(() -> service.clockIn(employee.getId(), longMemo))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    @Nested
    @DisplayName("退勤打刻にメモを付与")
    class ClockOutWithMemo {

        @Test
        @DisplayName("メモ付きで退勤打刻するとレスポンスにメモが含まれる")
        void clockOut_withMemo_returnsRecordWithMemo() {
            // Arrange
            var openRecord = AttendanceRecord.builder()
                    .id(UUID.randomUUID())
                    .employee(employee)
                    .workDate(TODAY_TOKYO)
                    .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                    .build();
            when(attendanceRepository.findByEmployeeIdAndWorkDateAndClockOutIsNull(employee.getId(), TODAY_TOKYO))
                    .thenReturn(Optional.of(openRecord));
            when(attendanceRepository.save(any(AttendanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.clockOut(employee.getId(), "直帰");

            // Assert
            assertThat(result.clockOutMemo()).isEqualTo("直帰");
        }

        @Test
        @DisplayName("メモなし（null）で退勤打刻できる")
        void clockOut_withoutMemo_returnsRecordWithNullMemo() {
            // Arrange
            var openRecord = AttendanceRecord.builder()
                    .id(UUID.randomUUID())
                    .employee(employee)
                    .workDate(TODAY_TOKYO)
                    .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                    .build();
            when(attendanceRepository.findByEmployeeIdAndWorkDateAndClockOutIsNull(employee.getId(), TODAY_TOKYO))
                    .thenReturn(Optional.of(openRecord));
            when(attendanceRepository.save(any(AttendanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.clockOut(employee.getId(), null);

            // Assert
            assertThat(result.clockOutMemo()).isNull();
        }

        @Test
        @DisplayName("メモが100文字を超える場合はバリデーションエラー")
        void clockOut_memoTooLong_throwsBadRequest() {
            // Arrange
            var openRecord = AttendanceRecord.builder()
                    .id(UUID.randomUUID())
                    .employee(employee)
                    .workDate(TODAY_TOKYO)
                    .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                    .build();
            when(attendanceRepository.findByEmployeeIdAndWorkDateAndClockOutIsNull(employee.getId(), TODAY_TOKYO))
                    .thenReturn(Optional.of(openRecord));
            var longMemo = "あ".repeat(101);

            // Act & Assert
            assertThatThrownBy(() -> service.clockOut(employee.getId(), longMemo))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    @Nested
    @DisplayName("メモ編集")
    class UpdateMemo {

        @Test
        @DisplayName("出勤メモを後から編集できる")
        void updateClockInMemo_validRequest_updatesSuccessfully() {
            // Arrange
            var recordId = UUID.randomUUID();
            var record = AttendanceRecord.builder()
                    .id(recordId)
                    .employee(employee)
                    .workDate(TODAY_TOKYO)
                    .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                    .build();
            when(attendanceRepository.findById(recordId)).thenReturn(Optional.of(record));
            when(attendanceRepository.save(any(AttendanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.updateMemo(recordId, employee.getId(), "客先訪問", null);

            // Assert
            assertThat(result.clockInMemo()).isEqualTo("客先訪問");
        }

        @Test
        @DisplayName("退勤メモを後から編集できる")
        void updateClockOutMemo_validRequest_updatesSuccessfully() {
            // Arrange
            var recordId = UUID.randomUUID();
            var record = AttendanceRecord.builder()
                    .id(recordId)
                    .employee(employee)
                    .workDate(TODAY_TOKYO)
                    .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                    .clockOut(Instant.parse("2025-01-15T08:00:00Z"))
                    .build();
            when(attendanceRepository.findById(recordId)).thenReturn(Optional.of(record));
            when(attendanceRepository.save(any(AttendanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.updateMemo(recordId, employee.getId(), null, "早退: 体調不良");

            // Assert
            assertThat(result.clockOutMemo()).isEqualTo("早退: 体調不良");
        }

        @Test
        @DisplayName("他人のレコードは編集できない（管理者以外）")
        void updateMemo_otherEmployee_throwsForbidden() {
            // Arrange
            var recordId = UUID.randomUUID();
            var otherEmployee = Employee.builder()
                    .id(UUID.randomUUID())
                    .name("鈴木花子")
                    .email("suzuki@example.com")
                    .password("hashed")
                    .department(employee.getDepartment())
                    .role(Role.EMPLOYEE)
                    .isManager(false)
                    .hireDate(LocalDate.of(2024, 4, 1))
                    .build();
            var record = AttendanceRecord.builder()
                    .id(recordId)
                    .employee(otherEmployee)
                    .workDate(TODAY_TOKYO)
                    .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                    .build();
            when(attendanceRepository.findById(recordId)).thenReturn(Optional.of(record));

            // Act & Assert
            assertThatThrownBy(() -> service.updateMemo(recordId, employee.getId(), "test", null))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.FORBIDDEN));
        }
    }
}
