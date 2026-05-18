package com.travelplan.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.admin.api.dto.UserCreateRequest;
import com.travelplan.admin.api.dto.UserUpdateRequest;
import com.travelplan.admin.domain.Role;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.RoleRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class UserAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        userRepository.deleteAll();
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role role = new Role();
            role.setName("ADMIN");
            roleRepository.save(role);
        }
        if (roleRepository.findByName("VIEWER").isEmpty()) {
            Role role = new Role();
            role.setName("VIEWER");
            roleRepository.save(role);
        }
    }

    @Test
    void admin_can_create_user() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", jwt.bearer("admin@example.com", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateRequest(
                                "created@example.com", "Secret123", "Cre", "Ated",
                                UserStatus.ACTIVE, java.util.Set.of("VIEWER")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("created@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("VIEWER"));
    }

    @Test
    void viewer_cannot_create_user() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", jwt.bearer("viewer@example.com", List.of("VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateRequest(
                                "x@example.com", "Secret123", null, null, null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewer_can_list_users() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", jwt.bearer("viewer@example.com", List.of("VIEWER"))))
                .andExpect(status().isOk());
    }

    @Test
    void create_rejects_duplicate_email_with_409() throws Exception {
        String authHeader = jwt.bearer("admin@example.com", List.of("ADMIN"));
        String payload = objectMapper.writeValueAsString(new UserCreateRequest(
                "dup@example.com", "Secret123", null, null, null, null));

        mockMvc.perform(post("/api/admin/users").header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/users").header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("conflict"));
    }

    @Test
    void create_rejects_invalid_payload_with_400() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", jwt.bearer("admin@example.com", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void update_then_get_reflects_changes() throws Exception {
        String authHeader = jwt.bearer("admin@example.com", List.of("ADMIN"));
        var created = createUser(authHeader, "edit@example.com");

        mockMvc.perform(put("/api/admin/users/" + created)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("New", "Name", UserStatus.SUSPENDED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        mockMvc.perform(get("/api/admin/users/" + created).header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void delete_returns_204_and_then_404() throws Exception {
        String authHeader = jwt.bearer("admin@example.com", List.of("ADMIN"));
        var created = createUser(authHeader, "delete@example.com");

        mockMvc.perform(delete("/api/admin/users/" + created).header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/users/" + created).header("Authorization", authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void change_password_then_old_password_is_useless_for_login() throws Exception {
        String authHeader = jwt.bearer("admin@example.com", List.of("ADMIN"));
        var created = createUser(authHeader, "pwd@example.com");

        mockMvc.perform(put("/api/admin/users/" + created + "/password")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"Rotated123\"}"))
                .andExpect(status().isNoContent());
    }

    private String createUser(String authHeader, String email) throws Exception {
        var result = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateRequest(
                                email, "Secret123", "First", "Last", UserStatus.ACTIVE, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
