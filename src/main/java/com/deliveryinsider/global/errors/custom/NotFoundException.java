package com.deliveryinsider.global.errors.custom;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message){
        super(message);
    }
}
