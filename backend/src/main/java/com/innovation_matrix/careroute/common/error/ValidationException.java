package com.innovation_matrix.careroute.common.error;

/** Input fails a business validation that bean validation annotations cannot express. */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
