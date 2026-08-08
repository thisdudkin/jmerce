/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NameTest {

    @Test
    void shouldNormalizeAndFormatName() {
        // Arrange
        String givenName = RandomTestData.alphabetic();
        String familyName = RandomTestData.alphabetic();
        String padding = RandomTestData.whitespace();

        // Act
        Name name = new Name(padding + givenName + padding, padding + familyName + padding);

        // Assert
        assertThat(name.givenName()).isEqualTo(givenName);
        assertThat(name.familyName()).isEqualTo(familyName);
        assertThat(name.fullName()).isEqualTo(givenName + " " + familyName);
    }

    @Test
    void shouldAcceptNamesAtMaximumLength() {
        // Arrange
        String maximumLengthName = RandomTestData.alphabetic(100);

        // Act
        Name name = new Name(maximumLengthName, maximumLengthName);

        // Assert
        assertThat(name.givenName()).isEqualTo(maximumLengthName);
        assertThat(name.familyName()).isEqualTo(maximumLengthName);
    }

    @ParameterizedTest
    @MethodSource("missingNames")
    void shouldRejectInvalidGivenName(String givenName) {
        // Arrange
        String validFamilyName = RandomTestData.alphabetic();

        // Act & Assert
        assertThatThrownBy(() -> new Name(givenName, validFamilyName))
            .isInstanceOf(DomainValidationException.class);
    }

    @ParameterizedTest
    @MethodSource("missingNames")
    void shouldRejectInvalidFamilyName(String familyName) {
        // Arrange
        String validGivenName = RandomTestData.alphabetic();

        // Act & Assert
        assertThatThrownBy(() -> new Name(validGivenName, familyName))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectNamesAboveMaximumLength() {
        // Arrange
        String validName = RandomTestData.alphabetic();
        String aboveMaximumLength = RandomTestData.alphabetic(101);

        // Act & Assert
        assertThatThrownBy(() -> new Name(aboveMaximumLength, validName))
            .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new Name(validName, aboveMaximumLength))
            .isInstanceOf(DomainValidationException.class);
    }

    private static Stream<String> missingNames() {
        return Stream.of(null, "", RandomTestData.whitespace());
    }

}
