package com.travelplan.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.Report;
import com.travelplan.admin.domain.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByReporterUserId(UUID reporterUserId);

    List<Report> findByStatus(ReportStatus status);

    long countByReporterUserId(UUID reporterUserId);

    @Query("SELECT COUNT(r) FROM Report r "
            + "WHERE r.targetType = com.travelplan.admin.domain.ReportTargetType.MANAGER AND r.targetId = :managerId")
    long countManagerReports(@Param("managerId") UUID managerId);

    @Query("SELECT r.targetId, COUNT(r) FROM Report r "
            + "WHERE r.targetType = com.travelplan.admin.domain.ReportTargetType.MANAGER GROUP BY r.targetId")
    List<Object[]> managerReportCountsGrouped();
}
