package com.agrosmart.iot.exception;

public class DeviceAlreadyRegisteredException extends RuntimeException {
    public DeviceAlreadyRegisteredException(String message)
    { super(message); }
}