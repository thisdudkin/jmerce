/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model;

import com.jmerce.customer.domain.exception.DomainValidationException;
import com.jmerce.customer.domain.model.value.AddressId;
import com.jmerce.customer.domain.model.value.AddressLine;
import com.jmerce.customer.domain.model.value.AddressPurpose;
import com.jmerce.customer.domain.model.value.AuditTimestamps;
import com.jmerce.customer.domain.model.value.City;
import com.jmerce.customer.domain.model.value.CountryCode;
import com.jmerce.customer.domain.model.value.CustomerId;
import com.jmerce.customer.domain.model.value.CustomerStatus;
import com.jmerce.customer.domain.model.value.Name;
import com.jmerce.customer.domain.model.value.PhoneNumber;
import com.jmerce.customer.domain.model.value.PostalCode;
import com.jmerce.customer.domain.model.value.RecipientName;
import com.jmerce.customer.domain.model.value.Region;
import com.jmerce.customer.domain.model.value.UserId;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Customer aggregate root.
 */
public final class Customer {

    private final CustomerId id;
    private final UserId userId;

    private Name name;
    private PhoneNumber phoneNumber;
    private CustomerStatus status;
    private AuditTimestamps timestamps;

    private final Set<Address> addresses;

    /**
     * Ctor.
     */
    private Customer(
        CustomerId id,
        UserId userId,
        Name name,
        PhoneNumber phoneNumber,
        CustomerStatus status,
        AuditTimestamps timestamps,
        Collection<Address> addresses
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.timestamps = timestamps;
        this.addresses = new LinkedHashSet<>(addresses);
    }

    /**
     * Factory.
     */
    public static Customer create(
        UserId userId,
        Name name,
        PhoneNumber phoneNumber,
        Instant timestamp
    ) {
        validateMandatoryFields(userId, name);
        validateTimestamp(timestamp);
        return new Customer(
            CustomerId.next(),
            userId,
            name,
            phoneNumber,
            CustomerStatus.ACTIVE,
            AuditTimestamps.created(timestamp),
            Set.of()
        );
    }

    /**
     * Restore from database.
     */
    public static Customer restore(
        CustomerId id,
        UserId userId,
        Name name,
        PhoneNumber phoneNumber,
        CustomerStatus status,
        AuditTimestamps timestamps,
        Collection<Address> addresses
    ) {
        if (id == null) {
            throw new DomainValidationException("Customer ID must not be null");
        }
        validateMandatoryFields(userId, name);
        if (status == null) {
            throw new DomainValidationException("Status must not be null");
        }
        if (timestamps == null) {
            throw new DomainValidationException("Timestamps must not be null");
        }
        if (addresses == null) {
            throw new DomainValidationException("Addresses must not be null");
        }
        validateAddresses(id, addresses);
        return new Customer(
            id,
            userId,
            name,
            phoneNumber,
            status,
            timestamps,
            addresses
        );
    }

    public void edit(
        Name name,
        PhoneNumber phoneNumber,
        Instant timestamp
    ) {
        if (name == null) {
            throw new DomainValidationException("Name can not be null");
        }
        validateTimestamp(timestamp);
        AuditTimestamps newTimestamps = timestamps.updated(timestamp);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.timestamps = newTimestamps;
    }

    public void suspend(Instant timestamp) {
        changeStatus(CustomerStatus.SUSPENDED, timestamp);
    }

    public void activate(Instant timestamp) {
        changeStatus(CustomerStatus.ACTIVE, timestamp);
    }

    public void close(Instant timestamp) {
        changeStatus(CustomerStatus.CLOSED, timestamp);
    }

    public AddressId addAddress(
        AddressPurpose purpose,
        RecipientName recipientName,
        AddressLine line1,
        AddressLine line2,
        City city,
        Region region,
        PostalCode postalCode,
        CountryCode countryCode,
        PhoneNumber phoneNumber,
        boolean defaultAddress,
        Instant timestamp
    ) {
        Address address = Address.create(
            id,
            purpose,
            recipientName,
            line1,
            line2,
            city,
            region,
            postalCode,
            countryCode,
            phoneNumber,
            defaultAddress,
            timestamp
        );
        Address existingDefault = null;
        if (defaultAddress) {
            existingDefault = findDefaultAddress(purpose);
            if (existingDefault != null) {
                existingDefault.validateUpdateTimestamp(timestamp);
            }
        }
        if (!addresses.add(address)) {
            throw new DomainValidationException("Duplicate address: " + address.getId());
        }
        if (existingDefault != null) {
            existingDefault.makeNonDefault(timestamp);
        }
        return address.getId();
    }

    public void editAddress(
        AddressId addressId,
        AddressPurpose purpose,
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
        if (purpose == null) {
            throw new DomainValidationException("Purpose must not be null");
        }
        Address address = requireAddress(addressId);
        if (address.isDefaultAddress() && !address.getPurpose().equals(purpose)) {
            Address existingDefault = findDefaultAddress(purpose);
            if (existingDefault != null && !existingDefault.getId().equals(addressId)) {
                throw new DomainValidationException("Default address already exists for purpose: " + purpose);
            }
        }
        address.edit(
            purpose,
            recipientName,
            line1,
            line2,
            city,
            region,
            postalCode,
            countryCode,
            phoneNumber,
            timestamp
        );
    }

    public void removeAddress(AddressId addressId) {
        Address address = requireAddress(addressId);
        addresses.remove(address);
    }

    public void makeAddressDefault(AddressId addressId, Instant timestamp) {
        Address target = requireAddress(addressId);
        if (target.isDefaultAddress()) {
            return;
        }
        Address currentDefault = findDefaultAddress(target.getPurpose());
        target.validateUpdateTimestamp(timestamp);
        if (currentDefault != null) {
            currentDefault.validateUpdateTimestamp(timestamp);
        }
        if (currentDefault != null) {
            currentDefault.makeNonDefault(timestamp);
        }
        target.makeDefault(timestamp);
    }

    public void makeAddressNonDefault(AddressId addressId, Instant timestamp) {
        Address address = requireAddress(addressId);
        if (!address.isDefaultAddress()) {
            return;
        }
        address.makeNonDefault(timestamp);
    }

    private void changeStatus(CustomerStatus target, Instant timestamp) {
        validateTimestamp(timestamp);
        CustomerStatus newStatus = status.transitionTo(target);
        AuditTimestamps updatedTimestamps = timestamps.updated(timestamp);
        this.status = newStatus;
        this.timestamps = updatedTimestamps;
    }

    private Address requireAddress(AddressId addressId) {
        if (addressId == null) {
            throw new DomainValidationException("Address ID must not be null");
        }
        return addresses.stream()
            .filter(address -> address.getId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new DomainValidationException(
                "Address %s does not belong to customer".formatted(addressId)
            ));
    }

    private Address findDefaultAddress(AddressPurpose purpose) {
        return addresses.stream()
            .filter(address -> address.getPurpose().equals(purpose))
            .filter(Address::isDefaultAddress)
            .findFirst()
            .orElse(null);
    }

    private static void validateMandatoryFields(UserId userId, Name name) {
        if (userId == null) {
            throw new DomainValidationException("User ID must not be null");
        }
        if (name == null) {
            throw new DomainValidationException("Name must not be null");
        }
    }

    private static void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new DomainValidationException("Timestamp must not be null");
        }
    }

    private static void validateAddresses(CustomerId customerId, Collection<Address> addresses) {
        Set<AddressId> addressIds = new HashSet<>();
        Set<AddressPurpose> defaultPurposes = new HashSet<>();
        for (Address address : addresses) {
            if (address == null) {
                throw new DomainValidationException("Address must not be null");
            }
            if (!customerId.equals(address.getCustomerId())) {
                throw new DomainValidationException(
                    "Address %s belongs to another customer".formatted(address.getId())
                );
            }
            if (!addressIds.add(address.getId())) {
                throw new DomainValidationException("Duplicate address: " + address.getId());
            }
            if (address.isDefaultAddress() && !defaultPurposes.add(address.getPurpose())) {
                throw new DomainValidationException(
                    "Multiple default addresses for purpose: " + address.getPurpose()
                );
            }
        }
    }

    public CustomerId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public Name getName() {
        return name;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public AuditTimestamps getTimestamps() {
        return timestamps;
    }

    public Set<Address> getAddresses() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(addresses));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Customer other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
