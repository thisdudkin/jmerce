/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.exception.DomainValidationException;
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
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Test
    void shouldCreateAddressWithGeneratedIdentityAndTimestamps() {
        // Arrange
        AddressData data = randomAddressData();

        // Act
        Address address = createNonDefaultAddress(data);

        // Assert
        assertThat(address.getId()).isNotNull();
        assertThat(address.getCustomerId()).isEqualTo(data.customerId());
        assertThat(address.getPurpose()).isEqualTo(AddressPurpose.SHIPPING);
        assertThat(address.getRecipientName()).isEqualTo(data.recipientName());
        assertThat(address.getLine1()).isEqualTo(data.line1());
        assertThat(address.getLine2()).isEqualTo(data.line2());
        assertThat(address.getCity()).isEqualTo(data.city());
        assertThat(address.getRegion()).isEqualTo(data.region());
        assertThat(address.getPostalCode()).isEqualTo(data.postalCode());
        assertThat(address.getCountryCode()).isEqualTo(data.countryCode());
        assertThat(address.getPhoneNumber()).isEqualTo(data.phoneNumber());
        assertThat(address.isDefaultAddress()).isFalse();
        assertThat(address.getTimestamps()).isEqualTo(AuditTimestamps.created(data.timestamp()));
    }

    @Test
    void shouldRestorePersistedAddress() {
        // Arrange
        AddressId addressId = DomainFixtures.addressId();
        CustomerId customerId = DomainFixtures.customerId();
        Instant createdAt = RandomTestData.instant();
        AuditTimestamps timestamps = new AuditTimestamps(createdAt, RandomTestData.after(createdAt));
        AddressPurpose purpose = AddressPurpose.BILLING;

        // Act
        Address address = DomainFixtures.restoredAddress(
            addressId,
            customerId,
            purpose,
            true,
            timestamps
        );

        // Assert
        assertThat(address.getId()).isEqualTo(addressId);
        assertThat(address.getCustomerId()).isEqualTo(customerId);
        assertThat(address.getPurpose()).isEqualTo(purpose);
        assertThat(address.isDefaultAddress()).isTrue();
        assertThat(address.getTimestamps()).isEqualTo(timestamps);
    }

    @Test
    void shouldEditAddressAndAdvanceTimestamp() {
        // Arrange
        AddressData original = randomAddressData();
        Address address = createNonDefaultAddress(original);
        AddressData updated = randomAddressDataAt(RandomTestData.after(original.timestamp()));

        // Act
        address.edit(
            AddressPurpose.BILLING,
            updated.recipientName(),
            updated.line1(),
            null,
            updated.city(),
            null,
            null,
            updated.countryCode(),
            null,
            updated.timestamp()
        );

        // Assert
        assertThat(address.getPurpose()).isEqualTo(AddressPurpose.BILLING);
        assertThat(address.getRecipientName()).isEqualTo(updated.recipientName());
        assertThat(address.getLine1()).isEqualTo(updated.line1());
        assertThat(address.getLine2()).isNull();
        assertThat(address.getRegion()).isNull();
        assertThat(address.getPostalCode()).isNull();
        assertThat(address.getPhoneNumber()).isNull();
        assertThat(address.getTimestamps())
            .isEqualTo(new AuditTimestamps(original.timestamp(), updated.timestamp()));
    }

    @Test
    void shouldNotPartiallyEditAddressWhenTimestampMovesBackwards() {
        // Arrange
        AddressData original = randomAddressData();
        Address address = createNonDefaultAddress(original);
        AddressData update = randomAddressDataAt(RandomTestData.before(original.timestamp()));

        // Act & Assert
        assertThatThrownBy(() -> address.edit(
            AddressPurpose.BILLING,
            update.recipientName(),
            update.line1(),
            update.line2(),
            update.city(),
            update.region(),
            update.postalCode(),
            update.countryCode(),
            update.phoneNumber(),
            update.timestamp()
        )).isInstanceOf(DomainValidationException.class);
        assertThat(address.getPurpose()).isEqualTo(AddressPurpose.SHIPPING);
        assertThat(address.getRecipientName()).isEqualTo(original.recipientName());
        assertThat(address.getTimestamps()).isEqualTo(AuditTimestamps.created(original.timestamp()));
    }

    @Test
    void shouldToggleDefaultFlagAndAdvanceTimestamp() {
        // Arrange
        AddressData data = randomAddressData();
        Address address = createNonDefaultAddress(data);
        Instant defaultedAt = RandomTestData.after(data.timestamp());
        Instant nonDefaultedAt = RandomTestData.after(defaultedAt);

        // Act
        address.makeDefault(defaultedAt);

        // Assert
        assertThat(address.isDefaultAddress()).isTrue();
        assertThat(address.getTimestamps().updatedAt()).isEqualTo(defaultedAt);

        // Act
        address.makeNonDefault(nonDefaultedAt);

        // Assert
        assertThat(address.isDefaultAddress()).isFalse();
        assertThat(address.getTimestamps().updatedAt()).isEqualTo(nonDefaultedAt);
    }

    @Test
    void shouldCompareAddressesByIdentity() {
        // Arrange
        AddressId addressId = DomainFixtures.addressId();
        CustomerId customerId = DomainFixtures.customerId();
        Instant createdAt = RandomTestData.instant();
        Address first = DomainFixtures.restoredAddress(
            addressId,
            customerId,
            AddressPurpose.SHIPPING,
            false,
            AuditTimestamps.created(createdAt)
        );
        Address second = DomainFixtures.restoredAddress(
            addressId,
            customerId,
            AddressPurpose.BILLING,
            true,
            new AuditTimestamps(createdAt, RandomTestData.after(createdAt))
        );

        // Act & Assert
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void shouldRejectInvalidRestoreState() {
        // Arrange
        AddressId addressId = DomainFixtures.addressId();
        CustomerId customerId = DomainFixtures.customerId();
        AuditTimestamps timestamps = AuditTimestamps.created(RandomTestData.instant());

        // Act & Assert
        assertThatThrownBy(() -> DomainFixtures.restoredAddress(
            null,
            customerId,
            AddressPurpose.SHIPPING,
            false,
            timestamps
        )).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> DomainFixtures.restoredAddress(
            addressId,
            null,
            AddressPurpose.SHIPPING,
            false,
            timestamps
        )).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> DomainFixtures.restoredAddress(
            addressId,
            customerId,
            AddressPurpose.SHIPPING,
            false,
            null
        )).isInstanceOf(DomainValidationException.class);
    }

    private static Address createNonDefaultAddress(AddressData data) {
        return Address.create(
            data.customerId(),
            AddressPurpose.SHIPPING,
            data.recipientName(),
            data.line1(),
            data.line2(),
            data.city(),
            data.region(),
            data.postalCode(),
            data.countryCode(),
            data.phoneNumber(),
            false,
            data.timestamp()
        );
    }

    private static AddressData randomAddressData() {
        return randomAddressDataAt(RandomTestData.instant());
    }

    private static AddressData randomAddressDataAt(Instant timestamp) {
        return new AddressData(
            DomainFixtures.customerId(),
            DomainFixtures.recipientName(),
            DomainFixtures.addressLine(),
            DomainFixtures.addressLine(),
            DomainFixtures.city(),
            DomainFixtures.region(),
            DomainFixtures.postalCode(),
            DomainFixtures.countryCode(),
            DomainFixtures.phoneNumber(),
            timestamp
        );
    }

    private record AddressData(
        CustomerId customerId,
        RecipientName recipientName,
        AddressLine line1,
        AddressLine line2,
        City city,
        Region region,
        PostalCode postalCode,
        CountryCode countryCode,
        PhoneNumber phoneNumber,
        Instant timestamp
    ) {
    }

}
