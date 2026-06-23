package com.deliveryinsider.global.errors.custom;

public class NotRegisteredStoreException extends RuntimeException{
    public NotRegisteredStoreException(String message){
        super(message);
    }
}
