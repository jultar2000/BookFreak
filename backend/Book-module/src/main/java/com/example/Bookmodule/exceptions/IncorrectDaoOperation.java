package com.example.Bookmodule.exceptions;

public class IncorrectDaoOperation extends RuntimeException{

    public IncorrectDaoOperation(String message, Exception exception) {
        super(message, exception);
    }

    public IncorrectDaoOperation(String message) {
        super(message);
    }
}
