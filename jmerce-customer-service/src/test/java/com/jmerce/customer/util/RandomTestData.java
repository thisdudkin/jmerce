/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.util;

import com.jmerce.customer.entity.Address;
import com.jmerce.customer.entity.Customer;
import com.jmerce.customer.enums.AddressPurpose;
import com.jmerce.customer.enums.CustomerStatus;
import com.jmerce.customer.rest.dto.AddressCreateRequest;
import com.jmerce.customer.rest.dto.AddressResponse;
import com.jmerce.customer.rest.dto.AddressUpdateRequest;
import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerDetailsResponse;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.rest.dto.CustomerUpdateRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomTestData {

    private static final int MIN_TEXT_LENGTH = 8;
    private static final int MAX_TEXT_LENGTH = 24;
    private static final int PHONE_NUMBER_LENGTH = 11;
    private static final long MAX_INSTANT_AGE_SECONDS = 31_536_000L;
    private static final long MAX_UPDATE_DELAY_SECONDS = 86_400L;

    private RandomTestData() {
    }

    public static UUID randomUuid() {
        return UUID.randomUUID();
    }

    public static String randomString() {
        return randomAlphabetic(MIN_TEXT_LENGTH, MAX_TEXT_LENGTH);
    }

    public static String randomPhoneNumber() {
        StringBuilder phoneNumber = new StringBuilder("+");
        phoneNumber.append(ThreadLocalRandom.current().nextInt(1, 10));
        for (int index = 1; index < PHONE_NUMBER_LENGTH; index++) {
            phoneNumber.append(ThreadLocalRandom.current().nextInt(10));
        }
        return phoneNumber.toString();
    }

    public static String randomCountryCode() {
        return randomAlphabetic(2, 2).toUpperCase();
    }

    public static Instant randomInstant() {
        long age = ThreadLocalRandom.current().nextLong(1, MAX_INSTANT_AGE_SECONDS);
        return Instant.now().minusSeconds(age);
    }

    public static CustomerCreateRequest randomCustomerCreateRequest() {
        return new CustomerCreateRequest(randomUuid(), randomString(), randomString())
            .phoneNumber(randomPhoneNumber());
    }

    public static CustomerUpdateRequest randomCustomerUpdateRequest() {
        return new CustomerUpdateRequest(randomString(), randomString())
            .phoneNumber(randomPhoneNumber());
    }

    public static CustomerResponse randomCustomerResponse() {
        Instant createdAt = randomInstant();
        Instant updatedAt = after(createdAt);
        return new CustomerResponse(
            randomUuid(),
            randomUuid(),
            randomString(),
            randomString(),
            randomPhoneNumber(),
            randomCustomerStatus(),
            createdAt,
            updatedAt
        );
    }

    public static CustomerDetailsResponse randomCustomerDetailsResponse() {
        UUID customerId = randomUuid();
        Instant createdAt = randomInstant();
        Instant updatedAt = after(createdAt);
        return new CustomerDetailsResponse(
            customerId,
            randomUuid(),
            randomString(),
            randomString(),
            randomPhoneNumber(),
            randomCustomerStatus(),
            createdAt,
            updatedAt,
            List.of(randomAddressResponse(customerId))
        );
    }

    public static AddressCreateRequest randomAddressCreateRequest() {
        return new AddressCreateRequest(
            randomAddressPurpose(),
            randomString(),
            randomString(),
            randomString(),
            randomCountryCode()
        )
            .line2(randomString())
            .region(randomString())
            .postalCode(randomString())
            .phoneNumber(randomPhoneNumber())
            .isDefault(ThreadLocalRandom.current().nextBoolean());
    }

    public static AddressUpdateRequest randomAddressUpdateRequest() {
        return new AddressUpdateRequest(
            randomAddressPurpose(),
            randomString(),
            randomString(),
            randomString(),
            randomCountryCode()
        )
            .line2(randomString())
            .region(randomString())
            .postalCode(randomString())
            .phoneNumber(randomPhoneNumber());
    }

    public static AddressResponse randomAddressResponse() {
        return randomAddressResponse(randomUuid());
    }

    public static AddressResponse randomAddressResponse(UUID customerId) {
        Instant createdAt = randomInstant();
        Instant updatedAt = after(createdAt);
        return new AddressResponse(
            randomAddressPurpose(),
            randomString(),
            randomString(),
            randomString(),
            randomCountryCode(),
            randomUuid(),
            customerId,
            ThreadLocalRandom.current().nextBoolean(),
            createdAt,
            updatedAt
        )
            .line2(randomString())
            .region(randomString())
            .postalCode(randomString())
            .phoneNumber(randomPhoneNumber());
    }

    public static Customer randomCustomer() {
        return randomCustomer(randomUuid(), randomCustomerStatus());
    }

    public static Customer randomCustomer(UUID customerId) {
        return randomCustomer(customerId, randomCustomerStatus());
    }

    public static Customer randomCustomer(CustomerStatus status) {
        return randomCustomer(randomUuid(), status);
    }

    public static Customer randomCustomer(UUID customerId, CustomerStatus status) {
        Instant createdAt = randomInstant();
        return Customer.builder()
            .id(customerId)
            .userId(randomUuid())
            .givenName(randomString())
            .familyName(randomString())
            .phoneNumber(randomPhoneNumber())
            .status(status)
            .createdAt(createdAt)
            .updatedAt(after(createdAt))
            .build();
    }

    public static Customer randomCustomerWithAddresses(UUID customerId, Address... addresses) {
        Customer customer = randomCustomer(customerId, randomCustomerStatus());
        for (Address address : addresses) {
            customer.addAddress(address);
        }
        return customer;
    }

    public static Address randomAddress(UUID addressId, AddressPurpose purpose, boolean isDefault) {
        Instant createdAt = randomInstant();
        return Address.builder()
            .id(addressId)
            .purpose(purpose)
            .recipientName(randomString())
            .line1(randomString())
            .line2(randomString())
            .city(randomString())
            .region(randomString())
            .postalCode(randomString())
            .countryCode(randomCountryCode())
            .phoneNumber(randomPhoneNumber())
            .isDefault(isDefault)
            .createdAt(createdAt)
            .updatedAt(after(createdAt))
            .build();
    }

    private static CustomerStatus randomCustomerStatus() {
        CustomerStatus[] statuses = CustomerStatus.values();
        return statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
    }

    private static AddressPurpose randomAddressPurpose() {
        AddressPurpose[] purposes = AddressPurpose.values();
        return purposes[ThreadLocalRandom.current().nextInt(purposes.length)];
    }

    private static Instant after(Instant instant) {
        long delay = ThreadLocalRandom.current().nextLong(1, MAX_UPDATE_DELAY_SECONDS);
        return instant.plusSeconds(delay);
    }

    private static String randomAlphabetic(int minLength, int maxLength) {
        int length = ThreadLocalRandom.current().nextInt(minLength, maxLength + 1);
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            result.append((char) ThreadLocalRandom.current().nextInt('a', 'z' + 1));
        }
        return result.toString();
    }

}
