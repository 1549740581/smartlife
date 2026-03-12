package com.yxtech.smartlife.common;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success");
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "internal server error");
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message);
    }

    public static <T> Result<T> badRequestWithData(String message, T data) {
        return new Result<>(400, message, data);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message);
    }

    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message);
    }
}
