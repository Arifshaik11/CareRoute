package com.innovation_matrix.careroute.common.error;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String entity, Object id) {
        super(entity + " " + id + " was not found");
    }
}
