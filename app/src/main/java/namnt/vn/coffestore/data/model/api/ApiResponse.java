package namnt.vn.coffestore.data.model.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse<T> {
    @SerializedName("status")
    private String status;

    @SerializedName("isSuccess")
    private Boolean isSuccess;

    @SerializedName("message")
    private String message = "";

    @SerializedName("errors")
    private List<ApiError> errors;

    @SerializedName("data")
    private T data;

    public boolean isSuccess() {
        if (isSuccess != null) {
            return isSuccess;
        }
        return "success".equalsIgnoreCase(status);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}


