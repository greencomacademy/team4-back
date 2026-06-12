package com.deliveryinsider.global.errors.custom;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){
        super(message);
    }
}
