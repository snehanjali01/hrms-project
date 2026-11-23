package com.hrms.security;

import com.hrms.config.CorsConfig;
import com.hrms.config.SecurityConfig;
import com.hrms.controller.AttendanceController;
import com.hrms.controller.UserController;
import com.hrms.service.AttendanceService;
import com.hrms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, AttendanceController.class})
@Import({SecurityConfig.class, JwtService.class, CorsConfig.class})
@TestPropertySource(properties = {
        "jwt.secret=ownership-rules-test-secret-with-more-than-32-chars",
        "jwt.expiration-ms=3600000"
})
class OwnershipRulesTest {

    private static final long MY_USER_ID = 5L;
    private static final long MY_EMPLOYEE_ID = 10L;
    private static final String PASSWORD_BODY = "{\"currentPassword\":\"old\",\"newPassword\":\"new\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AttendanceService attendanceService;

    private String bearer(String role) {
        return "Bearer " + jwtService.generateToken("someone", MY_USER_ID, MY_EMPLOYEE_ID, List.of(role));
    }

    @Test
    void changePassword_shouldReturn204_whenChangingOwnPassword() throws Exception {
        mockMvc.perform(put("/api/users/" + MY_USER_ID + "/change-password")
                        .header("Authorization", bearer("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PASSWORD_BODY))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(MY_USER_ID, "old", "new");
    }

    @Test
    void changePassword_shouldReturn403_whenChangingSomeoneElsesPassword() throws Exception {
        mockMvc.perform(put("/api/users/6/change-password")
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PASSWORD_BODY))
                .andExpect(status().isForbidden());

        verify(userService, never()).changePassword(6L, "old", "new");
    }

    @Test
    void attendance_shouldBeScopedToCaller_whenRoleIsEmployee() throws Exception {
        mockMvc.perform(get("/api/attendance").param("employeeId", "99")
                        .header("Authorization", bearer("EMPLOYEE")))
                .andExpect(status().isOk());

        verify(attendanceService).getAttendanceByEmployee(MY_EMPLOYEE_ID);
        verify(attendanceService, never()).getAttendanceByEmployee(99L);
        verify(attendanceService, never()).getAllAttendance();
    }

    @Test
    void attendance_shouldAllowQueryingAnyEmployee_whenRoleIsHr() throws Exception {
        mockMvc.perform(get("/api/attendance").param("employeeId", "99")
                        .header("Authorization", bearer("HR")))
                .andExpect(status().isOk());

        verify(attendanceService).getAttendanceByEmployee(99L);
    }
}
