package com.example.attendance.report.service;

import com.example.attendance.attendance.domain.WorkDuration;
import com.example.attendance.attendance.entity.AttendanceRecord;
import com.example.attendance.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.report.dto.EmployeeMonthlyRecord;
import com.example.attendance.report.dto.MonthlyReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public ReportServiceImpl(
            AttendanceRecordRepository attendanceRecordRepository,
            EmployeeRepository employeeRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public MonthlyReportResponse getMonthlyReport(String month, UUID departmentId) {
        var yearMonth = YearMonth.parse(month);
        var start = yearMonth.atDay(1);
        var end = yearMonth.atEndOfMonth();

        List<Employee> employees;
        if (departmentId != null) {
            employees = employeeRepository.findByDepartmentId(departmentId);
        } else {
            employees = employeeRepository.findAll();
        }

        var employeeIds = employees.stream().map(Employee::getId).toList();
        var allRecords = attendanceRecordRepository
                .findByEmployeeIdInAndWorkDateBetween(employeeIds, start, end);
        var byEmployee = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getEmployee().getId()));

        int weekdaysInMonth = countWeekdays(yearMonth);

        var records = employees.stream()
                .map(emp -> {
                    var empRecords = byEmployee.getOrDefault(emp.getId(), List.of());
                    var grouped = empRecords.stream()
                            .collect(Collectors.groupingBy(AttendanceRecord::getWorkDate));

                    int workDays = grouped.size();
                    int totalWorkMinutes = 0;
                    int totalOvertimeMinutes = 0;
                    for (var dayRecords : grouped.values()) {
                        var duration = WorkDuration.calculate(dayRecords);
                        totalWorkMinutes += duration.workMinutes();
                        totalOvertimeMinutes += duration.overtimeMinutes();
                    }
                    int absentDays = Math.max(0, weekdaysInMonth - workDays);

                    return new EmployeeMonthlyRecord(
                            emp.getId(),
                            emp.getName(),
                            emp.getDepartment().getName(),
                            workDays,
                            totalWorkMinutes,
                            totalOvertimeMinutes,
                            absentDays
                    );
                })
                .toList();

        return new MonthlyReportResponse(month, List.copyOf(records));
    }

    private int countWeekdays(YearMonth yearMonth) {
        int count = 0;
        var date = yearMonth.atDay(1);
        var end = yearMonth.atEndOfMonth();
        while (!date.isAfter(end)) {
            var dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
