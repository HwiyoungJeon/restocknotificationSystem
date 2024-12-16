package com.example.restocknotificationsysyem.global.exception;

import lombok.Getter;

@Getter
public class SuccessResponse<T> {
    private String message;
    private int code;
    private T data;

    public SuccessResponse(String message, int code, T data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }
}
