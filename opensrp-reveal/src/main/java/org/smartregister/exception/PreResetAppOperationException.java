package org.smartregister.exception;


public class PreResetAppOperationException extends Exception {

    public PreResetAppOperationException() {
        super();
    }

    public PreResetAppOperationException(String message) {
        super(message);
    }

    public PreResetAppOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreResetAppOperationException(Throwable cause) {
        super(cause);
    }
}
