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
import com.jmerce.customer.domain.model.value.PhoneNumber;
import com.jmerce.customer.domain.model.value.PostalCode;
import com.jmerce.customer.domain.model.value.RecipientName;
import com.jmerce.customer.domain.model.value.Region;

import java.time.Instant;

/**
 * Customer address.
 */
public final class Address {

    private final AddressId id;
    private final CustomerId customerId;

    private AddressPurpose purpose;
    private RecipientName recipientName;
    private AddressLine line1;
    private AddressLine line2;
    private City city;
    private Region region;
    private PostalCode postalCode;
    private CountryCode countryCode;
    private PhoneNumber phoneNumber;
    private boolean defaultAddress;
    private AuditTimestamps timestamps;

    /**
     * Ctor.
     */
    private Address(
        AddressId id,
        CustomerId customerId,
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
        AuditTimestamps timestamps
    ) {
        this.id = id;
        this.customerId = customerId;
        this.purpose = purpose;
        this.recipientName = recipientName;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.defaultAddress = defaultAddress;
        this.timestamps = timestamps;
    }

    /**
     * Factory.
     */
    static Address create(
        CustomerId customerId,
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
        if (customerId == null) {
            throw new DomainValidationException("Customer ID must not be null");
        }
        validateMandatoryFields(
            purpose,
            recipientName,
            line1,
            city,
            countryCode
        );
        validateTimestamp(timestamp);
        return new Address(
            AddressId.next(),
            customerId,
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
            AuditTimestamps.created(timestamp)
        );
    }

    /**
     * Restore from database.
     */
    public static Address restore(
        AddressId id,
        CustomerId customerId,
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
        AuditTimestamps timestamps
    ) {
        if (id == null) {
            throw new DomainValidationException("Customer ID must not be null");
        }
        if (customerId == null) {
            throw new DomainValidationException("Customer ID must not be null");
        }
        validateMandatoryFields(
            purpose,
            recipientName,
            line1,
            city,
            countryCode
        );
        if (timestamps == null) {
            throw new DomainValidationException("Timestamps must not be null");
        }
        return new Address(
            id,
            customerId,
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
            timestamps
        );
    }

    void edit(
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
        validateMandatoryFields(purpose, recipientName, line1, city, countryCode);
        AuditTimestamps newTimestamps = updatedTimestamps(timestamp);
        this.purpose = purpose;
        this.recipientName = recipientName;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.timestamps = newTimestamps;
    }

    void makeDefault(Instant timestamp) {
        AuditTimestamps newTimestamps = updatedTimestamps(timestamp);
        this.defaultAddress = true;
        this.timestamps = newTimestamps;
    }

    void makeNonDefault(Instant timestamp) {
        AuditTimestamps newTimestamps = updatedTimestamps(timestamp);
        this.defaultAddress = false;
        this.timestamps = newTimestamps;
    }

    void validateUpdateTimestamp(Instant timestamp) {
        updatedTimestamps(timestamp);
    }

    private AuditTimestamps updatedTimestamps(Instant timestamp) {
        validateTimestamp(timestamp);
        return timestamps.updated(timestamp);
    }

    private static void validateMandatoryFields(
        AddressPurpose purpose,
        RecipientName recipientName,
        AddressLine line1,
        City city,
        CountryCode countryCode
    ) {
        if (purpose == null) {
            throw new DomainValidationException("Purpose must not be null");
        }
        if (recipientName == null) {
            throw new DomainValidationException("Recipient name must not be null");
        }
        if (line1 == null) {
            throw new DomainValidationException("Address line 1 must not be null");
        }
        if (city == null) {
            throw new DomainValidationException("City must not be null");
        }
        if (countryCode == null) {
            throw new DomainValidationException("Country code must not be null");
        }
    }

    private static void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new DomainValidationException("Timestamp must not be null");
        }
    }

    public AddressId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public AddressPurpose getPurpose() {
        return purpose;
    }

    public RecipientName getRecipientName() {
        return recipientName;
    }

    public AddressLine getLine1() {
        return line1;
    }

    public AddressLine getLine2() {
        return line2;
    }

    public City getCity() {
        return city;
    }

    public Region getRegion() {
        return region;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public AuditTimestamps getTimestamps() {
        return timestamps;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Address other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
