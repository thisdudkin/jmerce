/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.rest.exception;

import com.jmerce.customer.exception.CustomerAlreadyExistsException;
import com.jmerce.customer.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException e) {
        log.debug("Entity not found: {}", e.getMessage());

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            e.getMessage()
        );
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ProblemDetail handleCustomerAlreadyExists(CustomerAlreadyExistsException e) {
        log.debug("Customer already exists: {}", e.getMessage());

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handle(Exception e) {
        log.error("Unexpected exception", e);

        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
