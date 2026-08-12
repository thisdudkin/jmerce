/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service.impl;

import com.jmerce.customer.entity.Address;
import com.jmerce.customer.entity.Customer;
import com.jmerce.customer.enums.AddressPurpose;
import com.jmerce.customer.exception.AddressNotFoundException;
import com.jmerce.customer.exception.CustomerNotFoundException;
import com.jmerce.customer.mapper.AddressMapper;
import com.jmerce.customer.repository.CustomerRepository;
import com.jmerce.customer.rest.dto.AddressCreateRequest;
import com.jmerce.customer.rest.dto.AddressResponse;
import com.jmerce.customer.rest.dto.AddressUpdateRequest;
import com.jmerce.customer.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(UUID customerId) {
        Customer customer = getCustomerWithAddresses(customerId);
        return customer.getAddresses()
            .stream()
            .map(addressMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public AddressResponse createAddress(UUID customerId, AddressCreateRequest request) {
        Customer customer = getCustomerForUpdate(customerId);
        Address address = addressMapper.toEntity(request);
        if (address.isDefault()) {
            boolean defaultAddressDemoted = demoteDefaultAddress(
                customer,
                address.getPurpose(),
                null
            );
            if (defaultAddressDemoted) {
                customerRepository.flush();
            }
        }
        customer.addAddress(address);
        customerRepository.saveAndFlush(customer);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddress(UUID customerId, UUID addressId) {
        Customer customer = getCustomerWithAddresses(customerId);
        Address address = findAddress(customer, addressId);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID customerId, UUID addressId, AddressUpdateRequest request) {
        Customer customer = getCustomerForUpdate(customerId);
        Address address = findAddress(customer, addressId);
        AddressPurpose previousPurpose = address.getPurpose();
        boolean wasDefault = address.isDefault();
        if (wasDefault) {
            address.setDefault(false);
        }
        addressMapper.update(address, request);
        if (wasDefault && previousPurpose != address.getPurpose()) {
            boolean targetPurposeDefaultDemoted = demoteDefaultAddress(
                customer,
                address.getPurpose(),
                address.getId()
            );
            if (targetPurposeDefaultDemoted) {
                customerRepository.flush();
            }
        }
        address.setDefault(wasDefault);
        customerRepository.saveAndFlush(customer);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID customerId, UUID addressId) {
        Customer customer = getCustomerForUpdate(customerId);
        Address address = findAddress(customer, addressId);
        customer.removeAddress(address);
        customerRepository.saveAndFlush(customer);
    }

    @Override
    @Transactional
    public AddressResponse makeDefault(UUID customerId, UUID addressId) {
        Customer customer = getCustomerForUpdate(customerId);
        Address address = findAddress(customer, addressId);
        if (address.isDefault()) {
            return addressMapper.toResponse(address);
        }
        boolean previousDefaultDemoted = demoteDefaultAddress(
            customer,
            address.getPurpose(),
            address.getId()
        );
        if (previousDefaultDemoted) {
            customerRepository.flush();
        }
        address.setDefault(true);
        customerRepository.saveAndFlush(customer);
        return addressMapper.toResponse(address);
    }

    private Customer getCustomerWithAddresses(UUID customerId) {
        return customerRepository.findWithAddressesById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private Customer getCustomerForUpdate(UUID customerId) {
        return customerRepository.findByIdForUpdate(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private Address findAddress(Customer customer, UUID addressId) {
        return customer.getAddresses()
            .stream()
            .filter(address -> addressId.equals(address.getId()))
            .findFirst()
            .orElseThrow(() -> new AddressNotFoundException(customer.getId(), addressId));
    }

    private boolean demoteDefaultAddress(Customer customer, AddressPurpose purpose, UUID excludedAddressId) {
        boolean demoted = false;
        for (Address candidate : customer.getAddresses()) {
            if (candidate.getPurpose() == purpose
                && candidate.isDefault()
                && !Objects.equals(candidate.getId(), excludedAddressId)) {
                candidate.setDefault(false);
                demoted = true;
            }
        }
        return demoted;
    }

}
