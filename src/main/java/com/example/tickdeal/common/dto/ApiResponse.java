package com.example.tickdeal.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final T data;

    @JsonPropertyOrder({"success", "code", "data"})
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, 200, null);
    }

    @JsonPropertyOrder({"success", "code", "data"})
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, data);
    }

    @JsonPropertyOrder({"success", "code", "data"})
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, 201, data);
    }

    @JsonPropertyOrder({"success", "code", "data"})
    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(true, 204, null);
    }

    @JsonPropertyOrder({"success", "code", "data"})
    public static <T> ApiResponse<T> fail(HttpStatus status, T data) {
        return new ApiResponse<>(false, status.value(), data);
    }

    @JsonPropertyOrder({"success", "code", "data"})
    public static <T> ApiResponse<PageResponse<T>> ok(Page<T> page) {
        return new ApiResponse<>(true, 200, PageResponse.of(page));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PageResponse<T> {

        private final List<T> content;
        private final PageInfo pageInfo;

        public static <T> PageResponse<T> of(Page<T> page) {
            return new PageResponse<>(page.getContent(), PageInfo.of(page));
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"page", "size", "totalElements", "totalPages", "isFirst", "isLast"})
    public static class PageInfo {

        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean isFirst;
        private final boolean isLast;

        public static PageInfo of(Page<?> page) {
            return new PageInfo(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast()
            );
        }
    }
}
