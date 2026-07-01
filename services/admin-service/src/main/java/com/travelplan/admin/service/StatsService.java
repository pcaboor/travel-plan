package com.travelplan.admin.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.ManagerDashboard;
import com.travelplan.admin.api.dto.ManagerScore;
import com.travelplan.admin.api.dto.TravelerStats;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.FeedbackRepository;
import com.travelplan.admin.repository.PaymentMethodRepository;
import com.travelplan.admin.repository.ReportRepository;

/**
 * Per-role statistics, computed locally from the denormalized bookings/feedback.
 * The manager score weighs rating, income and activity, penalized by reports:
 * {@code 0.5*rating + 0.3*income + 0.2*trips - 0.2*reports} (metrics normalized).
 */
@Service
@Transactional(readOnly = true)
public class StatsService {

    private static final Set<BookingStatus> PARTICIPATED =
            EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

    private final BookingRepository bookings;
    private final FeedbackRepository feedback;
    private final ReportRepository reports;
    private final PaymentMethodRepository paymentMethods;

    public StatsService(BookingRepository bookings, FeedbackRepository feedback,
                        ReportRepository reports, PaymentMethodRepository paymentMethods) {
        this.bookings = bookings;
        this.feedback = feedback;
        this.reports = reports;
        this.paymentMethods = paymentMethods;
    }

    public TravelerStats travelerStats(UUID userId) {
        long participations = bookings.countByUserIdAndStatusIn(userId, PARTICIPATED);
        long cancellations = bookings.countByUserIdAndStatus(userId, BookingStatus.CANCELLED);
        long reportsFiled = reports.countByReporterUserId(userId);
        long feedbackGiven = feedback.countByAuthorUserId(userId);
        List<String> providers = paymentMethods.findByUserId(userId).stream()
                .map(pm -> pm.getProvider().name())
                .distinct().sorted().toList();
        return new TravelerStats(participations, cancellations, reportsFiled, feedbackGiven, providers);
    }

    public ManagerDashboard managerDashboard(UUID managerId) {
        BigDecimal income = Optional.ofNullable(bookings.sumIncomeByManager(managerId, PARTICIPATED))
                .orElse(BigDecimal.ZERO);
        long travelers = bookings.countByManagerIdAndStatusIn(managerId, PARTICIPATED);
        long trips = bookings.countDistinctTravelsByManager(managerId);
        double avgRating = Optional.ofNullable(feedback.avgRatingByManager(managerId)).orElse(0.0);
        return new ManagerDashboard(income, trips, travelers, round(avgRating));
    }

    public List<ManagerScore> leaderboard() {
        Map<UUID, Agg> byManager = new HashMap<>();
        for (Object[] row : bookings.aggregateByManager(PARTICIPATED)) {
            Agg agg = byManager.computeIfAbsent((UUID) row[0], k -> new Agg());
            agg.income = (BigDecimal) row[1];
            agg.travelers = ((Number) row[2]).longValue();
            agg.trips = ((Number) row[3]).longValue();
        }
        for (Object[] row : feedback.avgRatingByManagerGrouped()) {
            byManager.computeIfAbsent((UUID) row[0], k -> new Agg()).avgRating = ((Number) row[1]).doubleValue();
        }
        for (Object[] row : reports.managerReportCountsGrouped()) {
            byManager.computeIfAbsent((UUID) row[0], k -> new Agg()).reports = ((Number) row[1]).longValue();
        }
        if (byManager.isEmpty()) {
            return List.of();
        }

        double minIncome = min(byManager, a -> a.income.doubleValue());
        double maxIncome = max(byManager, a -> a.income.doubleValue());
        double minTrips = min(byManager, a -> (double) a.trips);
        double maxTrips = max(byManager, a -> (double) a.trips);
        double minReports = min(byManager, a -> (double) a.reports);
        double maxReports = max(byManager, a -> (double) a.reports);

        List<ManagerScore> scores = new ArrayList<>();
        for (Map.Entry<UUID, Agg> e : byManager.entrySet()) {
            Agg a = e.getValue();
            double ratingNorm = a.avgRating / 5.0;
            double incomeNorm = norm(a.income.doubleValue(), minIncome, maxIncome);
            double tripsNorm = norm(a.trips, minTrips, maxTrips);
            double reportsNorm = norm(a.reports, minReports, maxReports);
            double score = 0.5 * ratingNorm + 0.3 * incomeNorm + 0.2 * tripsNorm - 0.2 * reportsNorm;
            scores.add(new ManagerScore(e.getKey(), a.income, a.trips, a.travelers,
                    round(a.avgRating), a.reports, round(score)));
        }
        scores.sort(Comparator.comparingDouble(ManagerScore::score).reversed());
        return scores;
    }

    private static double norm(double value, double min, double max) {
        if (max <= min) {
            return value > 0 ? 1.0 : 0.0;
        }
        return (value - min) / (max - min);
    }

    private static double min(Map<UUID, Agg> m, java.util.function.ToDoubleFunction<Agg> f) {
        return m.values().stream().mapToDouble(f).min().orElse(0.0);
    }

    private static double max(Map<UUID, Agg> m, java.util.function.ToDoubleFunction<Agg> f) {
        return m.values().stream().mapToDouble(f).max().orElse(0.0);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static final class Agg {
        private BigDecimal income = BigDecimal.ZERO;
        private long travelers;
        private long trips;
        private double avgRating;
        private long reports;
    }
}
