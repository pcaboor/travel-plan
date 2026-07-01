package com.travelplan.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.admin.api.dto.ReportCreateRequest;
import com.travelplan.admin.api.dto.ReportStatusUpdateRequest;
import com.travelplan.admin.domain.ReportStatus;
import com.travelplan.admin.domain.ReportTargetType;
import com.travelplan.admin.repository.ReportRepository;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportRepository reportRepository;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;
    private UUID reporterId;
    private String reporterAuth;
    private String adminAuth;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        reportRepository.deleteAll();
        reporterId = UUID.randomUUID();
        reporterAuth = "Bearer " + jwt.token(reporterId.toString(), "reporter@example.com", List.of("USER"));
        adminAuth = jwt.bearer("admin@example.com", List.of("ADMIN"));
    }

    @Test
    void traveler_can_file_a_report() throws Exception {
        mockMvc.perform(post("/api/reports").header("Authorization", reporterAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(ReportTargetType.MANAGER, UUID.randomUUID(), "Unprofessional")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.reporterUserId").value(reporterId.toString()));
    }

    @Test
    void report_requires_a_reason() throws Exception {
        mockMvc.perform(post("/api/reports").header("Authorization", reporterAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(ReportTargetType.TRAVEL, UUID.randomUUID(), null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void a_traveler_cannot_list_all_reports() throws Exception {
        mockMvc.perform(get("/api/reports").header("Authorization", reporterAuth))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_lists_reports() throws Exception {
        createReport();
        mockMvc.perform(get("/api/reports").header("Authorization", adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void reporter_lists_their_own_reports() throws Exception {
        createReport();
        mockMvc.perform(get("/api/reports/me").header("Authorization", reporterAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void admin_updates_a_report_status() throws Exception {
        String id = createReport();
        mockMvc.perform(post("/api/reports/{id}/status", id).header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReportStatusUpdateRequest(ReportStatus.REVIEWED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"));
    }

    @Test
    void a_traveler_cannot_update_a_report_status() throws Exception {
        String id = createReport();
        mockMvc.perform(post("/api/reports/{id}/status", id).header("Authorization", reporterAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReportStatusUpdateRequest(ReportStatus.DISMISSED))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updating_an_unknown_report_returns_404() throws Exception {
        mockMvc.perform(post("/api/reports/{id}/status", UUID.randomUUID()).header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReportStatusUpdateRequest(ReportStatus.ACTIONED))))
                .andExpect(status().isNotFound());
    }

    private String createReport() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reports").header("Authorization", reporterAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(ReportTargetType.MANAGER, UUID.randomUUID(), "Issue")))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String body(ReportTargetType type, UUID targetId, String reason) throws Exception {
        return objectMapper.writeValueAsString(new ReportCreateRequest(type, targetId, reason));
    }
}
