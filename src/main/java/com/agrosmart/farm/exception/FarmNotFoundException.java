package com.agrosmart.farm.exception;

public class FarmNotFoundException extends RuntimeException {
    public FarmNotFoundException(String message)
    { super(message); }
}