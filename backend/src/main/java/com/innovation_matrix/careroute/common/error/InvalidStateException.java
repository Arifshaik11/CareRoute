package com.innovation_matrix.careroute.common.error;

/** The request is well-formed but conflicts with the current state of a record. */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
