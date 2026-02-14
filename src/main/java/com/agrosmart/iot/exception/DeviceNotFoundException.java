package com.agrosmart.iot.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String message)
    { super(message); }
}