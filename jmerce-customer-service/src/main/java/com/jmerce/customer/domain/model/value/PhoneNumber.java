/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\+[1-9][0-9]{7,14}$");

    public PhoneNumber {
        if (value != null && !PATTERN.matcher(value).matches()) {
            throw new DomainValidationException("Phone number must be in international format");
        }
    }

}
