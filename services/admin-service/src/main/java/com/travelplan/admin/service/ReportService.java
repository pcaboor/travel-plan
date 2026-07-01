package com.travelplan.admin.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.ReportCreateRequest;
import com.travelplan.admin.api.dto.ReportResponse;
import com.travelplan.admin.domain.Report;
import com.travelplan.admin.domain.ReportStatus;
import com.travelplan.admin.repository.ReportRepository;

/**
 * Reports filed by travelers. Anyone (USER+) can file one; only admins list all
 * of them and move them through their lifecycle.
 */
@Service
@Transactional
public class ReportService {

    private final ReportRepository reports;

    public ReportService(ReportRepository reports) {
        this.reports = reports;
    }

    public ReportResponse create(UUID reporterUserId, ReportCreateRequest request) {
        Report report = new Report();
        report.setReporterUserId(reporterUserId);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason());
        report.setStatus(ReportStatus.OPEN);
        return ReportResponse.from(reports.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> list(ReportStatus status) {
        List<Report> found = (status == null) ? reports.findAll() : reports.findByStatus(status);
        return found.stream().map(ReportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> listMine(UUID reporterUserId) {
        return reports.findByReporterUserId(reporterUserId).stream().map(ReportResponse::from).toList();
    }

    public ReportResponse updateStatus(UUID id, ReportStatus status) {
        Report report = reports.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found: " + id));
        report.setStatus(status);
        return ReportResponse.from(reports.save(report));
    }
}
