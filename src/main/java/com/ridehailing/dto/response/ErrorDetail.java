package com.ridehailing.dto.response;

import java.util.List;

/**
 * Machine-readable error payload nested inside {@link ApiResponse#getError()} on failures.
 */
public class ErrorDetail {

    private String code;
    private List<String> details;

    public ErrorDetail() {
    }

    public ErrorDetail(String code, List<String> details) {
        this.code = code;
        this.details = details;
    }

    public static ErrorDetail of(String code, List<String> details) {
        return new ErrorDetail(code, details);
    }

    public static ErrorDetail of(String code, String singleDetail) {
        return new ErrorDetail(code, List.of(singleDetail));
    }

    public String getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}
