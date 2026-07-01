package com.travelplan.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.admin.domain.Report;
import com.travelplan.admin.domain.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByReporterUserId(UUID reporterUserId);

    List<Report> findByStatus(ReportStatus status);
}
