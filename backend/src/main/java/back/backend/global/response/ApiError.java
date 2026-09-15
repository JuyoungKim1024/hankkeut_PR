package back.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        String code,
        String message,
        List<FieldError> fields
) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError of(String code, String message, List<FieldError> fields) {
        return new ApiError(code, message, List.copyOf(fields));
    }

    public record FieldError(String field, String message) {
    }
}
