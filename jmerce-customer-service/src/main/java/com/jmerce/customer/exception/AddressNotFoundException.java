/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AddressNotFoundException extends EntityNotFoundException {
    public AddressNotFoundException(UUID customerId, UUID addressId) {
        super("Address %s not found for customer %s".formatted(addressId, customerId));
    }
}
