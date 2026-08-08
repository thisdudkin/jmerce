/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

public record PostalCode(String value) {

    public PostalCode {
        if (value == null) {
            throw new DomainValidationException("Postal code can not be null");
        }
        if (!value.equals(value.strip())) {
            throw new DomainValidationException("Postal code must not have leading or trailing whitespace");
        }
        if (value.isEmpty() || value.length() > 32) {
            throw new DomainValidationException("Postal code length must be between 1 and 32 characters");
        }
    }

}
