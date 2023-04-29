package com.xuecheng.base.exception;

/**
 * @autuor 范大晨
 * @Date 2023/4/29 15:24
 * @description 异常返回统一包装
 */
public class RestErrorResponse {
    private String message;

    public RestErrorResponse() {
    }

    public RestErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
