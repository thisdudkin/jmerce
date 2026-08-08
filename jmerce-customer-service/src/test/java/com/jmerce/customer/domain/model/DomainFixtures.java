/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.model.value.AddressId;
import com.jmerce.customer.domain.model.value.AddressLine;
import com.jmerce.customer.domain.model.value.AddressPurpose;
import com.jmerce.customer.domain.model.value.AuditTimestamps;
import com.jmerce.customer.domain.model.value.City;
import com.jmerce.customer.domain.model.value.CountryCode;
import com.jmerce.customer.domain.model.value.CustomerId;
import com.jmerce.customer.domain.model.value.PhoneNumber;
import com.jmerce.customer.domain.model.value.PostalCode;
import com.jmerce.customer.domain.model.value.RecipientName;
import com.jmerce.customer.domain.model.value.Region;
import com.jmerce.customer.domain.model.value.UserId;

final class DomainFixtures {

    private DomainFixtures() {
    }

    static Address restoredAddress(
        AddressId addressId,
        CustomerId customerId,
        AddressPurpose purpose,
        boolean defaultAddress,
        AuditTimestamps timestamps
    ) {
        return Address.restore(
            addressId,
            customerId,
            purpose,
            recipientName(),
            addressLine(),
            addressLine(),
            city(),
            region(),
            postalCode(),
            countryCode(),
            phoneNumber(),
            defaultAddress,
            timestamps
        );
    }

    static AddressId addressId() {
        return new AddressId(RandomTestData.uuid());
    }

    static CustomerId customerId() {
        return new CustomerId(RandomTestData.uuid());
    }

    static UserId userId() {
        return new UserId(RandomTestData.uuid());
    }

    static RecipientName recipientName() {
        return new RecipientName(RandomTestData.alphabetic());
    }

    static AddressLine addressLine() {
        return new AddressLine(RandomTestData.alphabetic());
    }

    static City city() {
        return new City(RandomTestData.alphabetic());
    }

    static Region region() {
        return new Region(RandomTestData.alphabetic());
    }

    static PostalCode postalCode() {
        return new PostalCode(RandomTestData.alphabetic());
    }

    static CountryCode countryCode() {
        return new CountryCode(RandomTestData.uppercaseAlphabetic(2));
    }

    static PhoneNumber phoneNumber() {
        return new PhoneNumber(RandomTestData.phoneNumber());
    }

}
