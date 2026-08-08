/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

public record RecipientName(String value) {

    public RecipientName {
        if (value == null) {
            throw new DomainValidationException("Recipient name must not be null");
        }
        if (!value.equals(value.strip())) {
            throw new DomainValidationException("Recipient name must not have leading or trailing whitespace");
        }
        if (value.isEmpty() || value.length() > 200) {
            throw new DomainValidationException("Recipient name length must be between 1 and 200 characters");
        }
    }

}
