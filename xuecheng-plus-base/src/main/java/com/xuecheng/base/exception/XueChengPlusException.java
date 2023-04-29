package com.xuecheng.base.exception;

/**
 * @autuor 范大晨
 * @Date 2023/4/29 14:57
 * @description 自定义业务异常
 */
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage=message;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }
}
