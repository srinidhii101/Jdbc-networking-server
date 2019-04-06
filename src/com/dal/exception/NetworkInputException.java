package com.dal.exception;

public class NetworkInputException extends Exception {
    public NetworkInputException(String invalid_operation) {
        super(invalid_operation);
    }
}
