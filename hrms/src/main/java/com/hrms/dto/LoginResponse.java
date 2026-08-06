package com.hrms.dto;

import com.hrms.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {

    private Long userId;
    private String username;
    private Long employeeId;
    private String employeeName;
    private List<String> roles;

    public static LoginResponse fromEntity(User user) {
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmployeeId(user.getEmployee().getId());
        response.setEmployeeName(
                user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());

        response.setRoles(
                user.getRoles() == null ? List.of()
                        : user.getRoles().stream().map(role -> role.getName()).toList());

        return response;
    }
}