package com.industrial.sim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private String traceId;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "成功", data, null);
    }
    
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }
    
    public static <T> ApiResponse<T> error(Integer code, String message, String traceId) {
        return new ApiResponse<>(code, message, null, traceId);
    }
}
