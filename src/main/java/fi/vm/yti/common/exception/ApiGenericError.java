package fi.vm.yti.common.exception;

import org.springframework.http.HttpStatus;

public class ApiGenericError extends ApiError {

    private String details;

    public ApiGenericError(HttpStatus status) {
        super(status);
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}

