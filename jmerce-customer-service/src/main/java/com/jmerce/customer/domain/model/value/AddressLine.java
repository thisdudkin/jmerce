/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

public record AddressLine(String value) {

    public AddressLine {
        if (value == null) {
            throw new DomainValidationException("Address line can not be null");
        }
        if (!value.equals(value.strip())) {
            throw new DomainValidationException("Address line must not have leading or trailing whitespace");
        }
        if (value.isEmpty() || value.length() > 300) {
            throw new DomainValidationException("Address line length must be between 1 and 300 characters");
        }
    }

}
