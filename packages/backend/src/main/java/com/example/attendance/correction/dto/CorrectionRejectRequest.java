package com.example.attendance.correction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CorrectionRejectRequest(
    @NotNull @Size(min = 1, max = 500) String reason,
    @NotNull Long version
) {}
