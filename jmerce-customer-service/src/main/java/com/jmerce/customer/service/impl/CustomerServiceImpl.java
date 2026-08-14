/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service.impl;

import com.jmerce.customer.entity.Customer;
import com.jmerce.customer.enums.CustomerStatus;
import com.jmerce.customer.exception.CustomerAlreadyExistsException;
import com.jmerce.customer.exception.CustomerNotFoundException;
import com.jmerce.customer.mapper.CustomerMapper;
import com.jmerce.customer.repository.CustomerRepository;
import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerDetailsResponse;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.rest.dto.CustomerUpdateRequest;
import com.jmerce.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByUserId(request.getUserId())) {
            throw new CustomerAlreadyExistsException(request.getUserId());
        }
        Customer customer = customerMapper.toEntity(request);
        customerRepository.save(customer);
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailsResponse getCustomer(UUID customerId) {
        return customerRepository.findWithAddressesById(customerId)
            .map(customerMapper::toDetailsResponse)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerUpdateRequest request) {
        Customer customer = loadCustomer(customerId);
        customerMapper.update(customer, request);
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse activateCustomer(UUID customerId) {
        return changeStatus(customerId, CustomerStatus.ACTIVE);
    }

    @Override
    @Transactional
    public CustomerResponse suspendCustomer(UUID customerId) {
        return changeStatus(customerId, CustomerStatus.SUSPENDED);
    }

    @Override
    @Transactional
    public CustomerResponse closeCustomer(UUID customerId) {
        return changeStatus(customerId, CustomerStatus.CLOSED);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID customerId) {
        Customer customer = loadCustomer(customerId);
        customerRepository.delete(customer);
    }

    private CustomerResponse changeStatus(UUID customerId, CustomerStatus status) {
        Customer customer = loadCustomer(customerId);
        customer.setStatus(status);
        return customerMapper.toResponse(customer);
    }

    private Customer loadCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

}
