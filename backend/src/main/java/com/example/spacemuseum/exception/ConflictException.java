package com.example.spacemuseum.exception;

/**
 * 业务冲突（HTTP 409）：例如设备已有未完成保养工单、工单状态不允许该流转。
 */
public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
