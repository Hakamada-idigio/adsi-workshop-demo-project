package com.example.attendance.attendance.dto;

import java.util.UUID;

public record ClockRequest(
    UUID employeeId,
    String memo
) {}
