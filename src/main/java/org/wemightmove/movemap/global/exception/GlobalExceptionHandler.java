package org.wemightmove.movemap.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler({CustomException.class})
    protected ResponseEntity<ErrorDto> handleCustomException(CustomException e, HttpServletRequest request) {
        ErrorDto errorDto = ErrorDto.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(e.getErrorCode().getStatus())
                .code(e.getErrorCode().getCode())
                .message(e.getErrorCode().getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorDto, HttpStatusCode.valueOf(e.getErrorCode().getStatus()));
    }

    /**
     * {@code @Validated} 컨트롤러의 파라미터 제약(@NotBlank/@Size 등) 위반을 400 으로 매핑한다.
     * (예: {@code /facilities/search} 의 빈 keyword) — 미처리 시 500 이 되는 것을 방지.
     */
    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .orElse(errorCode.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorDto, HttpStatusCode.valueOf(errorCode.getStatus()));
    }
}
