package org.wemightmove.movemap.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.exception.ErrorDto;

import java.io.IOException;
import java.time.LocalDateTime;


public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch(CustomException e) {
            ErrorCode errorCode = e.getErrorCode();
            response.setStatus(errorCode.getStatus());
            response.setContentType("application/json;charset=UTF-8");

            ErrorDto errorDto = ErrorDto.builder()
                    .timestamp(LocalDateTime.now().toString())
                    .status(errorCode.getStatus())
                    .code(errorCode.getCode())
                    .message(errorCode.getMessage())
                    .path(request.getRequestURI())
                    .build();
            String json = objectMapper.writeValueAsString(errorDto);
            response.getWriter().write(json);
        }
    }
}
