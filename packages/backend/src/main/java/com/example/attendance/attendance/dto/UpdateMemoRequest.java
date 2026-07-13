package com.example.attendance.attendance.dto;

import java.util.UUID;

public record UpdateMemoRequest(
    UUID requesterId,
    String clockInMemo,
    String clockOutMemo
) {}
