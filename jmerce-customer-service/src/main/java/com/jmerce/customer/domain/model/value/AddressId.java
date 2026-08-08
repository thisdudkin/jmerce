/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

import java.util.UUID;

public record AddressId(UUID value) {

    public AddressId {
        if (value == null) {
            throw new DomainValidationException("Address ID can not be null");
        }
    }

    public static AddressId next() {
        return new AddressId(UUID.randomUUID());
    }

}
