package com.hrms.dto;

import com.hrms.entity.Payroll;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Integer payPeriodMonth;
    private Integer payPeriodYear;
    private BigDecimal basicSalary;
    private BigDecimal deductions;
    private BigDecimal netSalary;

    public static PayrollResponse fromEntity(Payroll payroll) {
        PayrollResponse response = new PayrollResponse();
        response.setId(payroll.getId());
        response.setEmployeeId(payroll.getEmployee().getId());
        response.setEmployeeName(
                payroll.getEmployee().getFirstName() + " " + payroll.getEmployee().getLastName());
        response.setPayPeriodMonth(payroll.getPayPeriodMonth());
        response.setPayPeriodYear(payroll.getPayPeriodYear());
        response.setBasicSalary(payroll.getBasicSalary());
        response.setDeductions(payroll.getDeductions());
        response.setNetSalary(payroll.getNetSalary());
        return response;
    }
}