package com.xianguoji.server.common.exception;

import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException e, HttpServletRequest request) {
        String traceId = MDC.get("traceId");
        log.warn("[BIZ] traceId={} uri={} code={} msg={}", traceId, request.getRequestURI(),
                e.getResultCode().getCode(), e.getMessage());
        HttpStatus httpStatus = (e.getResultCode() == ResultCode.TOKEN_INVALID || e.getResultCode() == ResultCode.TOKEN_EXPIRED)
                ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(R.fail(e.getResultCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("参数错误");
        return R.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("参数错误");
        return R.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), "缺少参数: " + e.getParameterName());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNotFound(NoHandlerFoundException e) {
        return R.fail(ResultCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<Void> handleUnsupportedMedia(HttpMediaTypeNotSupportedException e) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), "不支持的Content-Type");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        String traceId = MDC.get("traceId");
        log.error("[UNHANDLED] traceId={} method={} uri={} type={} msg={}",
                traceId, request.getMethod(), request.getRequestURI(),
                e.getClass().getName(), e.getMessage(), e);
        // P2-1: 生产环境不暴露异常详情
        if ("prod".equals(activeProfile)) {
            return R.fail(ResultCode.INTERNAL_ERROR.getCode(), "服务器内部错误, traceId=" + traceId);
        }
        return R.fail(ResultCode.INTERNAL_ERROR.getCode(), e.getMessage() + ", traceId=" + traceId);
    }
}
