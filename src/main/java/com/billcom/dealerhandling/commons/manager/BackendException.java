package com.billcom.dealerhandling.commons.manager;

public class BackendException extends Exception{
    private static final long serialVersionUID = 1L;
    
    public BackendException(String message, Throwable t) {
        super(message, t);
    }
    public BackendException(String message) {
        super(message);
    }
}
