/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

import java.util.regex.Pattern;

public record CountryCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Z]{2}$");

    public CountryCode {
        if (value == null) {
            throw new DomainValidationException("Country code can not be null");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new DomainValidationException("Country code must be a two-letter uppercase code");
        }
    }

}
