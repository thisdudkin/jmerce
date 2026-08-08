/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

public record Name(String givenName, String familyName) {

    public Name {
        if (givenName == null) {
            throw new DomainValidationException("Given name must not be null");
        }
        if (familyName == null) {
            throw new DomainValidationException("Family name must not be null");
        }
        givenName = givenName.strip();
        familyName = familyName.strip();
        if (givenName.isEmpty() || givenName.length() > 100) {
            throw new DomainValidationException("Given name length must be between 1 and 100 characters");
        }
        if (familyName.isEmpty() || familyName.length() > 100) {
            throw new DomainValidationException("Family name length must be between 1 and 100 characters");
        }
    }

    public String fullName() {
        return "%s %s".formatted(givenName, familyName);
    }

}
