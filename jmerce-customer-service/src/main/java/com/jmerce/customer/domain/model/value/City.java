/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

public record City(String value) {

    public City {
        if (value == null) {
            throw new DomainValidationException("City can not be null");
        }
        if (!value.equals(value.strip())) {
            throw new DomainValidationException("City must not have leading or trailing whitespace");
        }
        if (value.isEmpty() || value.length() > 150) {
            throw new DomainValidationException("City length must be between 1 and 150 characters");
        }
    }

}
