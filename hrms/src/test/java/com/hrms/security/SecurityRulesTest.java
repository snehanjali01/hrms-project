package com.hrms.security;

import com.hrms.config.CorsConfig;
import com.hrms.config.SecurityConfig;
import com.hrms.controller.PayrollController;
import com.hrms.service.PayrollService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollController.class)
@Import({SecurityConfig.class, JwtService.class, CorsConfig.class})
@TestPropertySource(properties = {
        "jwt.secret=" + SecurityRulesTest.SECRET,
        "jwt.expiration-ms=3600000"
})
class SecurityRulesTest {

    static final String SECRET = "security-rules-test-secret-with-more-than-32-chars";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PayrollService payrollService;

    private String bearer(String role) {
        return "Bearer " + jwtService.generateToken("someone", 1L, 10L, List.of(role));
    }

    @Test
    void protectedEndpoint_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldReturn401_whenTokenIsGarbage() throws Exception {
        mockMvc.perform(get("/api/payroll").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldReturn401_whenTokenIsExpired() throws Exception {
        String expired = new JwtService(SECRET, -1_000L).generateToken("someone", 1L, 10L, List.of("HR"));

        mockMvc.perform(get("/api/payroll").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void payroll_shouldReturn403_whenRoleIsEmployee() throws Exception {
        mockMvc.perform(get("/api/payroll").header("Authorization", bearer("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void payroll_shouldReturn200_whenRoleIsHr() throws Exception {
        mockMvc.perform(get("/api/payroll").header("Authorization", bearer("HR")))
                .andExpect(status().isOk());
    }
}
