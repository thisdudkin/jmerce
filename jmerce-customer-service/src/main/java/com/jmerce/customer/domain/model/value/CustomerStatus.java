/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.IllegalStatusTransitionException;

import java.util.EnumSet;

public enum CustomerStatus {

    ACTIVE,
    SUSPENDED,
    CLOSED;

    public CustomerStatus transitionTo(CustomerStatus target) {
        if (!allowedTransitions().contains(target)) {
            throw new IllegalStatusTransitionException(this, target);
        }
        return target;
    }

    private EnumSet<CustomerStatus> allowedTransitions() {
        return switch (this) {
            case ACTIVE -> EnumSet.of(SUSPENDED, CLOSED);
            case SUSPENDED -> EnumSet.of(ACTIVE, CLOSED);
            case CLOSED -> EnumSet.noneOf(CustomerStatus.class);
        };
    }

}
