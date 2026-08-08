/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentifierValueObjectsTest {

    @Test
    void shouldCreateIdentifiersFromUuid() {
        // Arrange
        UUID value = RandomTestData.uuid();

        // Act
        CustomerId customerId = new CustomerId(value);
        AddressId addressId = new AddressId(value);
        UserId userId = new UserId(value);

        // Assert
        assertThat(customerId.value()).isEqualTo(value);
        assertThat(addressId.value()).isEqualTo(value);
        assertThat(userId.value()).isEqualTo(value);
    }

    @Test
    void shouldGenerateNonNullIdentifiers() {
        // Act
        CustomerId customerId = CustomerId.next();
        AddressId addressId = AddressId.next();

        // Assert
        assertThat(customerId.value()).isNotNull();
        assertThat(addressId.value()).isNotNull();
    }

    @Test
    void shouldCreateUserIdFromUuidString() {
        // Arrange
        UUID value = RandomTestData.uuid();

        // Act
        UserId userId = UserId.from(value.toString());

        // Assert
        assertThat(userId.value()).isEqualTo(value);
    }

    @Test
    void shouldRejectNullIdentifierValues() {
        // Act & Assert
        assertThatThrownBy(() -> new CustomerId(null))
            .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new AddressId(null))
            .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new UserId(null))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectMalformedUserId() {
        // Arrange
        String malformedUuid = RandomTestData.alphabetic();

        // Act & Assert
        assertThatThrownBy(() -> UserId.from(malformedUuid))
            .isInstanceOf(IllegalArgumentException.class);
    }

}
