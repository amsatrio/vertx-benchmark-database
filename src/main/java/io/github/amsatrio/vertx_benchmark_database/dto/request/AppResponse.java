package io.github.amsatrio.vertx_benchmark_database.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppResponse<T> {
    private int status;
    private String message;
    private String timestamp;
    private T data;

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }


    public String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
    
    public static <T> AppResponse<T> build(int status, String message, String timestamp, T data) {
        AppResponse<T> response = new AppResponse<>();
        response.setStatus(status);
        response.setMessage(message);
        response.setTimestamp(timestamp);
        response.setData(data);
        return response;
    }

    public static <T> AppResponse<T> success(T data) {
        AppResponse<T> response = new AppResponse<>();
        response.setStatus(200);
        response.setMessage("success");
        response.setTimestamp("");
        response.setData(data);
        return response;
    }

    public static <T> AppResponse<T> error(int status, String message) {
        AppResponse<T> response = new AppResponse<>();
        response.setStatus(status);
        response.setMessage(message);
        response.setTimestamp("");
        return response;
    }
}
