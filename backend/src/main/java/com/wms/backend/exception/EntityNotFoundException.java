package com.wms.backend.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends AppException {

    public EntityNotFoundException(String entity, Object id) {
        super(
                HttpStatus.NOT_FOUND,
                "ENTITY_NOT_FOUND",
                entity + " not found with id: " + id
        );
    }

    public EntityNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", message);
    }
}