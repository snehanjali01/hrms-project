package com.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PayrollBulkGenerateResponse {
    private int generated;
    private int skipped;
    private List<String> skippedReasons;
}