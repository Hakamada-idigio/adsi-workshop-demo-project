package com.example.attendance.correction.controller;

import com.example.attendance.correction.dto.CorrectionCreateRequest;
import com.example.attendance.correction.dto.CorrectionRejectRequest;
import com.example.attendance.correction.dto.CorrectionResponse;
import com.example.attendance.correction.dto.PendingCorrectionResponse;
import com.example.attendance.correction.entity.CorrectionStatus;
import com.example.attendance.correction.service.CorrectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/corrections")
public class CorrectionController {

    private final CorrectionService correctionService;

    public CorrectionController(CorrectionService correctionService) {
        this.correctionService = correctionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CorrectionResponse create(
            @RequestParam UUID requesterId,
            @Valid @RequestBody CorrectionCreateRequest request) {
        return correctionService.create(requesterId, request);
    }

    @GetMapping
    public List<CorrectionResponse> findByRequester(
            @RequestParam UUID requesterId,
            @RequestParam(required = false) CorrectionStatus status) {
        return correctionService.findByRequester(requesterId, status);
    }

    @GetMapping("/pending")
    public List<PendingCorrectionResponse> findPending(@RequestParam UUID managerId) {
        return correctionService.findPending(managerId);
    }

    @PatchMapping("/{id}/approve")
    public CorrectionResponse approve(
            @PathVariable UUID id,
            @RequestParam UUID approverId,
            @RequestParam Long version) {
        return correctionService.approve(id, approverId, version);
    }

    @PatchMapping("/{id}/reject")
    public CorrectionResponse reject(
            @PathVariable UUID id,
            @RequestParam UUID approverId,
            @Valid @RequestBody CorrectionRejectRequest request) {
        return correctionService.reject(id, approverId, request.reason(), request.version());
    }
}
