/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

public record Region(String value) {

    public Region {
        if (value == null) {
            throw new DomainValidationException("Region can not be null");
        }
        if (!value.equals(value.strip())) {
            throw new DomainValidationException("Region must not have leading or trailing whitespace");
        }
        if (value.isEmpty() || value.length() > 150) {
            throw new DomainValidationException("Region length must be between 1 and 150 characters");
        }
    }

}
