package com.deliveryinsider.global.errors.custom;

public class NotRegisteredException extends RuntimeException{
    public NotRegisteredException(String message){
        super(message);
    }
}
