package com.example.attendance.correction.service;

import com.example.attendance.correction.dto.CorrectionCreateRequest;
import com.example.attendance.correction.dto.CorrectionResponse;
import com.example.attendance.correction.dto.PendingCorrectionResponse;
import com.example.attendance.correction.entity.CorrectionStatus;

import java.util.List;
import java.util.UUID;

public interface CorrectionService {

    CorrectionResponse create(UUID requesterId, CorrectionCreateRequest request);

    List<CorrectionResponse> findByRequester(UUID requesterId, CorrectionStatus status);

    List<PendingCorrectionResponse> findPending(UUID managerId);

    CorrectionResponse approve(UUID correctionId, UUID approverId, Long version);

    CorrectionResponse reject(UUID correctionId, UUID approverId, String reason, Long version);
}
