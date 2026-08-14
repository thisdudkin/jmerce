/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.exception;

public abstract class EntityNotFoundException extends RuntimeException {
    protected EntityNotFoundException(String message) {
        super(message);
    }
}
