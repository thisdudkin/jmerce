/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.IllegalStatusTransitionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerStatusTest {

    @ParameterizedTest
    @CsvSource({
        "ACTIVE, SUSPENDED",
        "ACTIVE, CLOSED",
        "SUSPENDED, ACTIVE",
        "SUSPENDED, CLOSED"
    })
    void shouldAllowSupportedTransition(CustomerStatus current, CustomerStatus target) {
        // Act
        CustomerStatus result = current.transitionTo(target);

        // Assert
        assertThat(result).isEqualTo(target);
    }

    @ParameterizedTest
    @CsvSource({
        "ACTIVE, ACTIVE",
        "SUSPENDED, SUSPENDED",
        "CLOSED, ACTIVE",
        "CLOSED, SUSPENDED",
        "CLOSED, CLOSED"
    })
    void shouldRejectUnsupportedTransition(CustomerStatus current, CustomerStatus target) {
        // Act & Assert
        assertThatThrownBy(() -> current.transitionTo(target))
            .isInstanceOf(IllegalStatusTransitionException.class)
            .hasMessage("Customer status transition from %s to %s is not allowed", current, target);
    }

}
