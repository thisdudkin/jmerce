/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service.impl;

import com.jmerce.customer.entity.Customer;
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
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
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
        Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        customerMapper.update(existingCustomer, request);
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID customerId) {
        customerRepository.deleteById(customerId);
    }

}
