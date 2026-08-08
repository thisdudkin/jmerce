/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.exception.DomainValidationException;
import com.jmerce.customer.domain.exception.IllegalStatusTransitionException;
import com.jmerce.customer.domain.model.value.AddressId;
import com.jmerce.customer.domain.model.value.AddressPurpose;
import com.jmerce.customer.domain.model.value.AuditTimestamps;
import com.jmerce.customer.domain.model.value.CustomerId;
import com.jmerce.customer.domain.model.value.CustomerStatus;
import com.jmerce.customer.domain.model.value.Name;
import com.jmerce.customer.domain.model.value.PhoneNumber;
import com.jmerce.customer.domain.model.value.RecipientName;
import com.jmerce.customer.domain.model.value.UserId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Nested
    class Creation {

        @Test
        void shouldCreateActiveCustomerWithoutAddresses() {
            // Arrange
            CustomerData data = randomCustomerData();

            // Act
            Customer customer = createCustomer(data);

            // Assert
            assertThat(customer.getId()).isNotNull();
            assertThat(customer.getUserId()).isEqualTo(data.userId());
            assertThat(customer.getName()).isEqualTo(data.name());
            assertThat(customer.getPhoneNumber()).isEqualTo(data.phoneNumber());
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getTimestamps()).isEqualTo(AuditTimestamps.created(data.createdAt()));
            assertThat(customer.getAddresses()).isEmpty();
        }

        @Test
        void shouldRejectMissingMandatoryCreationData() {
            // Arrange
            CustomerData data = randomCustomerData();

            // Act & Assert
            assertThatThrownBy(() -> Customer.create(
                null,
                data.name(),
                data.phoneNumber(),
                data.createdAt()
            )).isInstanceOf(DomainValidationException.class);
            assertThatThrownBy(() -> Customer.create(
                data.userId(),
                null,
                data.phoneNumber(),
                data.createdAt()
            )).isInstanceOf(DomainValidationException.class);
            assertThatThrownBy(() -> Customer.create(
                data.userId(),
                data.name(),
                data.phoneNumber(),
                null
            )).isInstanceOf(DomainValidationException.class);
        }

    }

    @Nested
    class Restoration {

        @Test
        void shouldRestoreCustomerAndDefensivelyCopyAddresses() {
            // Arrange
            CustomerData data = randomCustomerData();
            CustomerId customerId = DomainFixtures.customerId();
            Address address = restoredAddress(
                DomainFixtures.addressId(),
                customerId,
                AddressPurpose.SHIPPING,
                true,
                data.createdAt()
            );
            List<Address> source = new ArrayList<>(List.of(address));
            Instant updatedAt = RandomTestData.after(data.createdAt());

            // Act
            Customer customer = Customer.restore(
                customerId,
                data.userId(),
                data.name(),
                data.phoneNumber(),
                CustomerStatus.SUSPENDED,
                new AuditTimestamps(data.createdAt(), updatedAt),
                source
            );
            source.clear();

            // Assert
            assertThat(customer.getId()).isEqualTo(customerId);
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
            assertThat(customer.getAddresses()).containsExactly(address);
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldRejectAddressOwnedByAnotherCustomer() {
            // Arrange
            CustomerId customerId = DomainFixtures.customerId();
            Address address = restoredAddress(
                DomainFixtures.addressId(),
                DomainFixtures.customerId(),
                AddressPurpose.SHIPPING,
                false,
                RandomTestData.instant()
            );

            // Act & Assert
            assertThatThrownBy(() -> restore(customerId, List.of(address)))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldRejectDuplicateAddressIdentity() {
            // Arrange
            CustomerId customerId = DomainFixtures.customerId();
            AddressId addressId = DomainFixtures.addressId();
            Instant timestamp = RandomTestData.instant();
            Address first = restoredAddress(
                addressId,
                customerId,
                AddressPurpose.SHIPPING,
                false,
                timestamp
            );
            Address duplicate = restoredAddress(
                addressId,
                customerId,
                AddressPurpose.BILLING,
                false,
                timestamp
            );

            // Act & Assert
            assertThatThrownBy(() -> restore(customerId, List.of(first, duplicate)))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldRejectMultipleDefaultAddressesForSamePurpose() {
            // Arrange
            CustomerId customerId = DomainFixtures.customerId();
            Instant timestamp = RandomTestData.instant();
            Address first = restoredAddress(
                DomainFixtures.addressId(),
                customerId,
                AddressPurpose.SHIPPING,
                true,
                timestamp
            );
            Address second = restoredAddress(
                DomainFixtures.addressId(),
                customerId,
                AddressPurpose.SHIPPING,
                true,
                timestamp
            );

            // Act & Assert
            assertThatThrownBy(() -> restore(customerId, List.of(first, second)))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldRejectMissingMandatoryRestoreData() {
            // Arrange
            CustomerData data = randomCustomerData();
            CustomerId customerId = DomainFixtures.customerId();
            AuditTimestamps timestamps = AuditTimestamps.created(data.createdAt());

            // Act & Assert
            assertThatThrownBy(() -> Customer.restore(
                null,
                data.userId(),
                data.name(),
                data.phoneNumber(),
                CustomerStatus.ACTIVE,
                timestamps,
                Set.of()
            )).isInstanceOf(DomainValidationException.class);
            assertThatThrownBy(() -> Customer.restore(
                customerId,
                data.userId(),
                data.name(),
                data.phoneNumber(),
                null,
                timestamps,
                Set.of()
            )).isInstanceOf(DomainValidationException.class);
            assertThatThrownBy(() -> Customer.restore(
                customerId,
                data.userId(),
                data.name(),
                data.phoneNumber(),
                CustomerStatus.ACTIVE,
                null,
                Set.of()
            )).isInstanceOf(DomainValidationException.class);
            assertThatThrownBy(() -> Customer.restore(
                customerId,
                data.userId(),
                data.name(),
                data.phoneNumber(),
                CustomerStatus.ACTIVE,
                timestamps,
                null
            )).isInstanceOf(DomainValidationException.class);
        }

    }

    @Nested
    class ProfileEditing {

        @Test
        void shouldEditCustomerProfileAndAdvanceTimestamp() {
            // Arrange
            CustomerData data = randomCustomerData();
            Customer customer = createCustomer(data);
            Name newName = randomName();
            Instant updatedAt = RandomTestData.after(data.createdAt());

            // Act
            customer.edit(newName, null, updatedAt);

            // Assert
            assertThat(customer.getName()).isEqualTo(newName);
            assertThat(customer.getPhoneNumber()).isNull();
            assertThat(customer.getTimestamps())
                .isEqualTo(new AuditTimestamps(data.createdAt(), updatedAt));
        }

        @Test
        void shouldNotPartiallyEditProfileWhenTimestampMovesBackwards() {
            // Arrange
            CustomerData data = randomCustomerData();
            Customer customer = createCustomer(data);
            Name newName = randomName();
            Instant invalidTimestamp = RandomTestData.before(data.createdAt());

            // Act & Assert
            assertThatThrownBy(() -> customer.edit(newName, null, invalidTimestamp))
                .isInstanceOf(DomainValidationException.class);
            assertThat(customer.getName()).isEqualTo(data.name());
            assertThat(customer.getPhoneNumber()).isEqualTo(data.phoneNumber());
            assertThat(customer.getTimestamps()).isEqualTo(AuditTimestamps.created(data.createdAt()));
        }

    }

    @Nested
    class StatusChanges {

        @Test
        void shouldSupportValidStatusLifecycle() {
            // Arrange
            CustomerData data = randomCustomerData();
            Customer customer = createCustomer(data);
            Instant suspendedAt = RandomTestData.after(data.createdAt());
            Instant reactivatedAt = RandomTestData.after(suspendedAt);
            Instant closedAt = RandomTestData.after(reactivatedAt);

            // Act
            customer.suspend(suspendedAt);
            customer.activate(reactivatedAt);
            customer.close(closedAt);

            // Assert
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.CLOSED);
            assertThat(customer.getTimestamps().updatedAt()).isEqualTo(closedAt);
        }

        @Test
        void shouldNotChangeStateAfterIllegalStatusTransition() {
            // Arrange
            CustomerData data = randomCustomerData();
            Customer customer = createCustomer(data);
            Instant timestamp = RandomTestData.after(data.createdAt());

            // Act & Assert
            assertThatThrownBy(() -> customer.activate(timestamp))
                .isInstanceOf(IllegalStatusTransitionException.class);
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getTimestamps()).isEqualTo(AuditTimestamps.created(data.createdAt()));
        }

        @Test
        void shouldNotChangeStateWhenStatusTimestampMovesBackwards() {
            // Arrange
            CustomerData data = randomCustomerData();
            Customer customer = createCustomer(data);
            Instant invalidTimestamp = RandomTestData.before(data.createdAt());

            // Act & Assert
            assertThatThrownBy(() -> customer.suspend(invalidTimestamp))
                .isInstanceOf(DomainValidationException.class);
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getTimestamps()).isEqualTo(AuditTimestamps.created(data.createdAt()));
        }

    }

    @Nested
    class AddressManagement {

        @Test
        void shouldAddAddressAndReturnItsIdentity() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant timestamp = RandomTestData.instant();

            // Act
            AddressId addressId = addAddress(customer, AddressPurpose.SHIPPING, false, timestamp);

            // Assert
            Address address = requireAddress(customer, addressId);
            assertThat(address.getCustomerId()).isEqualTo(customer.getId());
            assertThat(address.getPurpose()).isEqualTo(AddressPurpose.SHIPPING);
            assertThat(address.isDefaultAddress()).isFalse();
        }

        @Test
        void shouldReplaceDefaultOnlyWithinSamePurpose() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant createdAt = RandomTestData.instant();
            Instant replacedAt = RandomTestData.after(createdAt);
            AddressId oldShippingId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                true,
                createdAt
            );
            AddressId billingId = addAddress(customer, AddressPurpose.BILLING, true, createdAt);

            // Act
            AddressId newShippingId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                true,
                replacedAt
            );

            // Assert
            assertThat(requireAddress(customer, oldShippingId).isDefaultAddress()).isFalse();
            assertThat(requireAddress(customer, oldShippingId).getTimestamps().updatedAt())
                .isEqualTo(replacedAt);
            assertThat(requireAddress(customer, newShippingId).isDefaultAddress()).isTrue();
            assertThat(requireAddress(customer, billingId).isDefaultAddress()).isTrue();
        }

        @Test
        void shouldNotAddDefaultAddressWhenExistingDefaultCannotBeUpdated() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant currentTimestamp = RandomTestData.instant();
            Instant invalidTimestamp = RandomTestData.before(currentTimestamp);
            AddressId currentDefaultId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                true,
                currentTimestamp
            );

            // Act & Assert
            assertThatThrownBy(() -> addAddress(
                customer,
                AddressPurpose.SHIPPING,
                true,
                invalidTimestamp
            )).isInstanceOf(DomainValidationException.class);
            assertThat(customer.getAddresses()).hasSize(1);
            assertThat(requireAddress(customer, currentDefaultId).isDefaultAddress()).isTrue();
        }

        @Test
        void shouldEditOwnedAddress() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant createdAt = RandomTestData.instant();
            Instant updatedAt = RandomTestData.after(createdAt);
            AddressId addressId = addAddress(customer, AddressPurpose.SHIPPING, false, createdAt);
            RecipientName recipientName = DomainFixtures.recipientName();

            // Act
            customer.editAddress(
                addressId,
                AddressPurpose.BILLING,
                recipientName,
                DomainFixtures.addressLine(),
                null,
                DomainFixtures.city(),
                null,
                null,
                DomainFixtures.countryCode(),
                null,
                updatedAt
            );

            // Assert
            Address address = requireAddress(customer, addressId);
            assertThat(address.getPurpose()).isEqualTo(AddressPurpose.BILLING);
            assertThat(address.getRecipientName()).isEqualTo(recipientName);
            assertThat(address.getTimestamps().updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldRejectChangingDefaultAddressToPurposeWithAnotherDefault() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant createdAt = RandomTestData.instant();
            AddressId shippingId = addAddress(customer, AddressPurpose.SHIPPING, true, createdAt);
            addAddress(customer, AddressPurpose.BILLING, true, createdAt);
            Address original = requireAddress(customer, shippingId);
            RecipientName originalRecipientName = original.getRecipientName();

            // Act & Assert
            assertThatThrownBy(() -> customer.editAddress(
                shippingId,
                AddressPurpose.BILLING,
                DomainFixtures.recipientName(),
                DomainFixtures.addressLine(),
                null,
                DomainFixtures.city(),
                null,
                null,
                DomainFixtures.countryCode(),
                null,
                RandomTestData.after(createdAt)
            )).isInstanceOf(DomainValidationException.class);
            assertThat(original.getPurpose()).isEqualTo(AddressPurpose.SHIPPING);
            assertThat(original.getRecipientName()).isEqualTo(originalRecipientName);
        }

        @Test
        void shouldMakeAddressDefaultAndDemoteCurrentDefault() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant createdAt = RandomTestData.instant();
            Instant defaultedAt = RandomTestData.after(createdAt);
            AddressId currentDefaultId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                true,
                createdAt
            );
            AddressId targetId = addAddress(customer, AddressPurpose.SHIPPING, false, createdAt);

            // Act
            customer.makeAddressDefault(targetId, defaultedAt);

            // Assert
            assertThat(requireAddress(customer, currentDefaultId).isDefaultAddress()).isFalse();
            assertThat(requireAddress(customer, targetId).isDefaultAddress()).isTrue();
            assertThat(requireAddress(customer, currentDefaultId).getTimestamps().updatedAt())
                .isEqualTo(defaultedAt);
            assertThat(requireAddress(customer, targetId).getTimestamps().updatedAt())
                .isEqualTo(defaultedAt);
        }

        @Test
        void shouldNotChangeDefaultsWhenTimestampMovesBackwards() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant currentTimestamp = RandomTestData.instant();
            Instant invalidTimestamp = RandomTestData.before(currentTimestamp);
            AddressId currentDefaultId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                true,
                currentTimestamp
            );
            AddressId targetId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                false,
                currentTimestamp
            );

            // Act & Assert
            assertThatThrownBy(() -> customer.makeAddressDefault(targetId, invalidTimestamp))
                .isInstanceOf(DomainValidationException.class);
            assertThat(requireAddress(customer, currentDefaultId).isDefaultAddress()).isTrue();
            assertThat(requireAddress(customer, targetId).isDefaultAddress()).isFalse();
        }

        @Test
        void shouldMakeDefaultAddressNonDefault() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant createdAt = RandomTestData.instant();
            Instant updatedAt = RandomTestData.after(createdAt);
            AddressId addressId = addAddress(customer, AddressPurpose.SHIPPING, true, createdAt);

            // Act
            customer.makeAddressNonDefault(addressId, updatedAt);

            // Assert
            assertThat(requireAddress(customer, addressId).isDefaultAddress()).isFalse();
            assertThat(requireAddress(customer, addressId).getTimestamps().updatedAt())
                .isEqualTo(updatedAt);
        }

        @Test
        void shouldRemoveOwnedAddress() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            AddressId addressId = addAddress(
                customer,
                AddressPurpose.SHIPPING,
                false,
                RandomTestData.instant()
            );

            // Act
            customer.removeAddress(addressId);

            // Assert
            assertThat(customer.getAddresses()).isEmpty();
        }

        @Test
        void shouldRejectMissingOrUnknownAddressIdentity() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            AddressId unknownAddressId = DomainFixtures.addressId();

            // Act & Assert
            assertThatThrownBy(() -> customer.removeAddress(null))
                .isInstanceOf(DomainValidationException.class);
            assertThatThrownBy(() -> customer.removeAddress(unknownAddressId))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void shouldExposeUnmodifiableAddressSnapshot() {
            // Arrange
            Customer customer = createCustomer(randomCustomerData());
            Instant timestamp = RandomTestData.instant();
            AddressId firstId = addAddress(customer, AddressPurpose.SHIPPING, false, timestamp);
            Set<Address> snapshot = customer.getAddresses();

            // Act & Assert
            assertThatThrownBy(snapshot::clear)
                .isInstanceOf(UnsupportedOperationException.class);

            // Act
            AddressId secondId = addAddress(customer, AddressPurpose.BILLING, false, timestamp);

            // Assert
            assertThat(snapshot).extracting(Address::getId).containsExactly(firstId);
            assertThat(customer.getAddresses()).extracting(Address::getId)
                .containsExactly(firstId, secondId);
        }

    }

    @Test
    void shouldCompareCustomersByIdentity() {
        // Arrange
        CustomerId customerId = DomainFixtures.customerId();
        Customer first = restore(customerId, List.of());
        CustomerData secondData = randomCustomerData();
        Customer second = Customer.restore(
            customerId,
            secondData.userId(),
            secondData.name(),
            null,
            CustomerStatus.SUSPENDED,
            AuditTimestamps.created(secondData.createdAt()),
            List.of()
        );

        // Act & Assert
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    private static Customer createCustomer(CustomerData data) {
        return Customer.create(
            data.userId(),
            data.name(),
            data.phoneNumber(),
            data.createdAt()
        );
    }

    private static Customer restore(CustomerId customerId, List<Address> addresses) {
        CustomerData data = randomCustomerData();
        return Customer.restore(
            customerId,
            data.userId(),
            data.name(),
            data.phoneNumber(),
            CustomerStatus.ACTIVE,
            AuditTimestamps.created(data.createdAt()),
            addresses
        );
    }

    private static Address restoredAddress(
        AddressId addressId,
        CustomerId customerId,
        AddressPurpose purpose,
        boolean defaultAddress,
        Instant timestamp
    ) {
        return DomainFixtures.restoredAddress(
            addressId,
            customerId,
            purpose,
            defaultAddress,
            AuditTimestamps.created(timestamp)
        );
    }

    private static AddressId addAddress(
        Customer customer,
        AddressPurpose purpose,
        boolean defaultAddress,
        Instant timestamp
    ) {
        return customer.addAddress(
            purpose,
            DomainFixtures.recipientName(),
            DomainFixtures.addressLine(),
            DomainFixtures.addressLine(),
            DomainFixtures.city(),
            DomainFixtures.region(),
            DomainFixtures.postalCode(),
            DomainFixtures.countryCode(),
            DomainFixtures.phoneNumber(),
            defaultAddress,
            timestamp
        );
    }

    private static Address requireAddress(Customer customer, AddressId addressId) {
        return customer.getAddresses().stream()
            .filter(address -> address.getId().equals(addressId))
            .findFirst()
            .orElseThrow();
    }

    private static CustomerData randomCustomerData() {
        return new CustomerData(
            DomainFixtures.userId(),
            randomName(),
            DomainFixtures.phoneNumber(),
            RandomTestData.instant()
        );
    }

    private static Name randomName() {
        return new Name(RandomTestData.alphabetic(), RandomTestData.alphabetic());
    }

    private record CustomerData(
        UserId userId,
        Name name,
        PhoneNumber phoneNumber,
        Instant createdAt
    ) {
    }

}
