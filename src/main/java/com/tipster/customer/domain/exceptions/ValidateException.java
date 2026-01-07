package com.tipster.customer.domain.exceptions;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidateException extends RuntimeException{
    private final List<String> errors;
    public ValidateException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
}
