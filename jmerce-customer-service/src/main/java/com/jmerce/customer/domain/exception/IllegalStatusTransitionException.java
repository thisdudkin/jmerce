/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.exception;

import com.jmerce.customer.domain.model.value.CustomerStatus;

public final class IllegalStatusTransitionException extends DomainValidationException {
    public IllegalStatusTransitionException(CustomerStatus current, CustomerStatus target) {
        super("Customer status transition from %s to %s is not allowed".formatted(current, target));
    }
}
