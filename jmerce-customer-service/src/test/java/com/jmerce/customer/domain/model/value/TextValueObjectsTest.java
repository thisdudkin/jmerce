/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextValueObjectsTest {

    @ParameterizedTest(name = "{0} accepts values at its length boundaries")
    @MethodSource("boundedTypes")
    void shouldAcceptValidBoundedValues(
        String type,
        Function<String, Object> factory,
        int maximumLength
    ) {
        // Arrange
        String minimumLengthValue = RandomTestData.alphabetic(1);
        String maximumLengthValue = RandomTestData.alphabetic(maximumLength);

        // Act & Assert
        assertThatCode(() -> factory.apply(minimumLengthValue))
            .as(type)
            .doesNotThrowAnyException();
        assertThatCode(() -> factory.apply(maximumLengthValue))
            .as(type)
            .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} rejects {1}")
    @MethodSource("invalidBoundedValues")
    void shouldRejectInvalidBoundedValues(
        String type,
        String scenario,
        Function<String, Object> factory,
        String value
    ) {
        // Act & Assert
        assertThatThrownBy(() -> factory.apply(value))
            .as("%s: %s", type, scenario)
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldAcceptUppercaseIsoCountryCode() {
        // Arrange
        String value = RandomTestData.uppercaseAlphabetic(2);

        // Act
        CountryCode countryCode = new CountryCode(value);

        // Assert
        assertThat(countryCode.value()).isEqualTo(value);
    }

    @ParameterizedTest(name = "rejects country code: {0}")
    @MethodSource("invalidCountryCodes")
    void shouldRejectInvalidCountryCode(String value) {
        // Act & Assert
        assertThatThrownBy(() -> new CountryCode(value))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectNullCountryCode() {
        // Act & Assert
        assertThatThrownBy(() -> new CountryCode(null))
            .isInstanceOf(DomainValidationException.class);
    }

    @ParameterizedTest(name = "accepts a phone number containing {0} digits")
    @MethodSource("validPhoneNumberLengths")
    void shouldAcceptPhoneNumberAtLengthBoundaries(int digitCount) {
        // Arrange
        String value = RandomTestData.phoneNumber(digitCount);

        // Act
        PhoneNumber phoneNumber = new PhoneNumber(value);

        // Assert
        assertThat(phoneNumber.value()).isEqualTo(value);
    }

    @Test
    void shouldAllowMissingPhoneNumber() {
        // Act
        PhoneNumber phoneNumber = new PhoneNumber(null);

        // Assert
        assertThat(phoneNumber.value()).isNull();
    }

    @ParameterizedTest(name = "rejects phone number: {0}")
    @MethodSource("invalidPhoneNumbers")
    void shouldRejectInvalidPhoneNumber(String value) {
        // Act & Assert
        assertThatThrownBy(() -> new PhoneNumber(value))
            .isInstanceOf(DomainValidationException.class);
    }

    private static Stream<Arguments> boundedTypes() {
        return boundedTypeDefinitions().stream()
            .map(type -> Arguments.of(type.name(), type.factory(), type.maximumLength()));
    }

    private static Stream<Arguments> invalidBoundedValues() {
        return boundedTypeDefinitions().stream()
            .flatMap(type -> {
                String validValue = RandomTestData.alphabetic();
                return Stream.of(
                    Arguments.of(type.name(), "null", type.factory(), null),
                    Arguments.of(type.name(), "an empty value", type.factory(), ""),
                    Arguments.of(type.name(), "a blank value", type.factory(), RandomTestData.whitespace()),
                    Arguments.of(type.name(), "leading whitespace", type.factory(), " " + validValue),
                    Arguments.of(type.name(), "trailing whitespace", type.factory(), validValue + " "),
                    Arguments.of(
                        type.name(),
                        "a value above the maximum length",
                        type.factory(),
                        RandomTestData.alphabetic(type.maximumLength() + 1)
                    )
                );
            });
    }

    private static List<BoundedType> boundedTypeDefinitions() {
        return List.of(
            new BoundedType("AddressLine", AddressLine::new, 300),
            new BoundedType("City", City::new, 150),
            new BoundedType("PostalCode", PostalCode::new, 32),
            new BoundedType("RecipientName", RecipientName::new, 200),
            new BoundedType("Region", Region::new, 150)
        );
    }

    private static Stream<String> invalidCountryCodes() {
        String validCode = RandomTestData.uppercaseAlphabetic(2);
        return Stream.of(
            "",
            RandomTestData.uppercaseAlphabetic(1),
            RandomTestData.uppercaseAlphabetic(3),
            validCode.toLowerCase(),
            RandomTestData.alphabetic(1) + RandomTestData.phoneNumber(8).charAt(1),
            " " + validCode
        );
    }

    private static Stream<Integer> validPhoneNumberLengths() {
        return Stream.of(8, 15);
    }

    private static Stream<String> invalidPhoneNumbers() {
        String validNumber = RandomTestData.phoneNumber();
        return Stream.of(
            "",
            validNumber.substring(1),
            "+0" + validNumber.substring(2),
            RandomTestData.phoneNumber(7),
            RandomTestData.phoneNumber(16),
            validNumber.substring(0, 4) + " " + validNumber.substring(4)
        );
    }

    private record BoundedType(
        String name,
        Function<String, Object> factory,
        int maximumLength
    ) {
    }

}
