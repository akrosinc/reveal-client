package org.smartregister.exception;

import java.io.IOException;


public class NoHttpResponseException extends IOException {

    public NoHttpResponseException(String message) {
        super(message);
    }
}
