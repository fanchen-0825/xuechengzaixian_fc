package com.xuecheng.base.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @autuor 范大晨
 * @Date 2023/4/29 15:08
 * @description 统一异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse serviceException(XueChengPlusException exception){
        return new RestErrorResponse(exception.getErrMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse unknowException(Exception exception){
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }
}
