/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service;

import com.jmerce.customer.entity.Address;
import com.jmerce.customer.entity.Customer;
import com.jmerce.customer.enums.AddressPurpose;
import com.jmerce.customer.mapper.AddressMapper;
import com.jmerce.customer.repository.CustomerRepository;
import com.jmerce.customer.rest.dto.AddressCreateRequest;
import com.jmerce.customer.rest.dto.AddressResponse;
import com.jmerce.customer.rest.dto.AddressUpdateRequest;
import com.jmerce.customer.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.jmerce.customer.util.RandomTestData.randomAddress;
import static com.jmerce.customer.util.RandomTestData.randomAddressCreateRequest;
import static com.jmerce.customer.util.RandomTestData.randomAddressResponse;
import static com.jmerce.customer.util.RandomTestData.randomAddressUpdateRequest;
import static com.jmerce.customer.util.RandomTestData.randomCustomerWithAddresses;
import static com.jmerce.customer.util.RandomTestData.randomUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTests {

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void shouldReplaceDefaultAddressWhenCreating() {
        // Arrange
        UUID customerId = randomUuid();
        Address current = randomAddress(randomUuid(), AddressPurpose.SHIPPING, true);
        Address replacement = randomAddress(null, AddressPurpose.SHIPPING, true);
        Customer customer = randomCustomerWithAddresses(customerId, current);
        AddressCreateRequest request = randomAddressCreateRequest();
        AddressResponse response = randomAddressResponse(customerId);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toEntity(request)).thenReturn(replacement);
        when(addressMapper.toResponse(replacement)).thenReturn(response);

        // Act
        AddressResponse result = addressService.createAddress(customerId, request);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(current.isDefault()).isFalse();
        assertThat(replacement.getCustomer()).isSameAs(customer);
        verify(customerRepository, times(2)).flush();
    }

    @Test
    void shouldCreateNonDefaultAddress() {
        // Arrange
        UUID customerId = randomUuid();
        Address address = randomAddress(null, AddressPurpose.SHIPPING, false);
        Customer customer = randomCustomerWithAddresses(customerId);
        AddressCreateRequest request = randomAddressCreateRequest();
        AddressResponse response = randomAddressResponse(customerId);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toEntity(request)).thenReturn(address);
        when(addressMapper.toResponse(address)).thenReturn(response);

        // Act
        AddressResponse result = addressService.createAddress(customerId, request);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(customer.getAddresses()).containsExactly(address);
        verify(customerRepository).flush();
    }

    @Test
    void shouldPreserveDefaultWhenChangingAddressPurpose() {
        // Arrange
        UUID customerId = randomUuid();
        Address address = randomAddress(randomUuid(), AddressPurpose.SHIPPING, true);
        Address currentBilling = randomAddress(randomUuid(), AddressPurpose.BILLING, true);
        Customer customer = randomCustomerWithAddresses(customerId, address, currentBilling);
        AddressUpdateRequest request = randomAddressUpdateRequest();
        AddressResponse response = randomAddressResponse(customerId);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        doAnswer(invocation -> {
            address.setPurpose(AddressPurpose.BILLING);
            return null;
        }).when(addressMapper).update(address, request);
        when(addressMapper.toResponse(address)).thenReturn(response);

        // Act
        AddressResponse result = addressService.updateAddress(customerId, address.getId(), request);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(address.isDefault()).isTrue();
        assertThat(currentBilling.isDefault()).isFalse();
        verify(customerRepository, times(2)).flush();
    }

    @Test
    void shouldUpdateNonDefaultAddress() {
        // Arrange
        UUID customerId = randomUuid();
        Address address = randomAddress(randomUuid(), AddressPurpose.SHIPPING, false);
        Customer customer = randomCustomerWithAddresses(customerId, address);
        AddressUpdateRequest request = randomAddressUpdateRequest();
        AddressResponse response = randomAddressResponse(customerId);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toResponse(address)).thenReturn(response);

        // Act
        AddressResponse result = addressService.updateAddress(customerId, address.getId(), request);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(address.isDefault()).isFalse();
        verify(customerRepository).flush();
    }

    @Test
    void shouldMakeAddressDefaultWithoutReplacement() {
        // Arrange
        UUID customerId = randomUuid();
        Address address = randomAddress(randomUuid(), AddressPurpose.SHIPPING, false);
        Address billing = randomAddress(randomUuid(), AddressPurpose.BILLING, true);
        Customer customer = randomCustomerWithAddresses(customerId, address, billing);
        AddressResponse response = randomAddressResponse(customerId);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toResponse(address)).thenReturn(response);

        // Act
        AddressResponse result = addressService.makeDefault(customerId, address.getId());

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(address.isDefault()).isTrue();
        assertThat(billing.isDefault()).isTrue();
        verify(customerRepository).flush();
    }

    @Test
    void shouldNotWriteWhenAddressIsAlreadyDefault() {
        // Arrange
        UUID customerId = randomUuid();
        Address address = randomAddress(randomUuid(), AddressPurpose.BILLING, true);
        Customer customer = randomCustomerWithAddresses(customerId, address);
        AddressResponse response = randomAddressResponse(customerId);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toResponse(address)).thenReturn(response);

        // Act
        AddressResponse result = addressService.makeDefault(customerId, address.getId());

        // Assert
        assertThat(result).isSameAs(response);
        verify(customerRepository, never()).flush();
    }

    @Test
    void shouldDeleteAddress() {
        // Arrange
        UUID customerId = randomUuid();
        Address address = randomAddress(randomUuid(), AddressPurpose.SHIPPING, false);
        Customer customer = randomCustomerWithAddresses(customerId, address);
        when(customerRepository.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));

        // Act
        addressService.deleteAddress(customerId, address.getId());

        // Assert
        assertThat(customer.getAddresses()).doesNotContain(address);
        assertThat(address.getCustomer()).isNull();
        verify(customerRepository).flush();
    }

}
