package com.deliveryinsider.global.errors.custom;

public class HttpMessageNotReadableException extends RuntimeException{
    public HttpMessageNotReadableException(String message){
        super(message);
    }
}
