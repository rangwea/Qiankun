package com.wikia.calabash.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author wikia
 * @since 2019/7/23 11:30
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<?> mvcExceptionHandler(HttpServletRequest req, HttpRequestMethodNotSupportedException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<?> mvcExceptionHandler(HttpServletRequest req, HttpMediaTypeNotSupportedException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<?> mvcExceptionHandler(HttpServletRequest req, HttpMediaTypeNotAcceptableException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> mvcExceptionHandler(HttpServletRequest req, MissingPathVariableException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> mvcExceptionHandler(HttpServletRequest req, MissingServletRequestParameterException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> mvcExceptionHandler(HttpServletRequest req, MissingRequestHeaderException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<?> mvcExceptionHandler(HttpServletRequest req, NoHandlerFoundException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> mvcExceptionHandler(HttpServletRequest req, HttpMessageNotReadableException e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> argumentNotValidExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException e) {
        return mvcErrorInfo(req, Arrays.toString(e.getBindingResult().getFieldErrors()
                        .stream()
                        .map(error -> error.getField() + ":" + error.getDefaultMessage())
                        .toArray()
                )
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> duplicateKeyExceptionHandler(HttpServletRequest req, DuplicateKeyException e) {
        return mvcErrorInfo(req, "已经存在相同配置的数据");
    }

    @ExceptionHandler(Forbidden.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> forbiddenExceptionHandler(HttpServletRequest req, Forbidden e) {
        return mvcErrorInfo(req, e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> defaultErrorHandler(HttpServletRequest req, Exception e) {
        R<?> errorInfo = new R<>(CommonError.SERVER_ERROR.code(), CommonError.SERVER_ERROR.message());
        errorInfo.setDetail(e.getMessage());

        String requestURI = req.getRequestURI();
        String method = req.getMethod();
        String queryString = req.getQueryString();
        log.error("server error:uri={};method={};query={};errorInfo={}", requestURI, method, queryString, errorInfo, e);

        return errorInfo;
    }

    private R<?> mvcErrorInfo(HttpServletRequest req, String detail) {
        R<?> errorInfo = new R<>(CommonError.REQUEST_ERROR.code(), CommonError.REQUEST_ERROR.message(), detail);

        String requestURI = req.getRequestURI();
        String method = req.getMethod();
        String queryString = req.getQueryString();
        log.error("server error:uri={};method={};query={};errorInfo={}", requestURI, method, queryString, errorInfo);

        return errorInfo;
    }
}
