package com.example.attendance.correction.repository;

import com.example.attendance.correction.entity.AttendanceCorrection;
import com.example.attendance.correction.entity.CorrectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, UUID> {

    List<AttendanceCorrection> findByRequesterIdOrderByCreatedAtDesc(UUID requesterId);

    List<AttendanceCorrection> findByRequesterIdAndStatusOrderByCreatedAtDesc(
            UUID requesterId, CorrectionStatus status);

    List<AttendanceCorrection> findByRequesterDepartmentIdAndStatusOrderByCreatedAtDesc(
            UUID departmentId, CorrectionStatus status);
}
