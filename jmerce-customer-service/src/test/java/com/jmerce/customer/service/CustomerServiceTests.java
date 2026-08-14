/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service;

import com.jmerce.customer.entity.Customer;
import com.jmerce.customer.enums.CustomerStatus;
import com.jmerce.customer.exception.CustomerAlreadyExistsException;
import com.jmerce.customer.exception.CustomerNotFoundException;
import com.jmerce.customer.mapper.CustomerMapper;
import com.jmerce.customer.repository.CustomerRepository;
import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.jmerce.customer.util.RandomTestData.randomCustomer;
import static com.jmerce.customer.util.RandomTestData.randomCustomerCreateRequest;
import static com.jmerce.customer.util.RandomTestData.randomCustomerResponse;
import static com.jmerce.customer.util.RandomTestData.randomUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void shouldCreateCustomer() {
        // Arrange
        CustomerCreateRequest request = randomCustomerCreateRequest();
        UUID userId = request.getUserId();
        Customer customer = randomCustomer();
        CustomerResponse response = randomCustomerResponse();
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.createCustomer(request);

        // Assert
        assertThat(result).isSameAs(response);
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectDuplicateCustomer() {
        // Arrange
        CustomerCreateRequest request = randomCustomerCreateRequest();
        UUID userId = request.getUserId();
        when(customerRepository.existsByUserId(userId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
            .isInstanceOf(CustomerAlreadyExistsException.class);
        verifyNoInteractions(customerMapper);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldActivateCustomer() {
        // Arrange
        UUID customerId = randomUuid();
        Customer customer = randomCustomer(customerId, CustomerStatus.SUSPENDED);
        CustomerResponse response = randomCustomerResponse();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.activateCustomer(customerId);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void shouldSuspendCustomer() {
        // Arrange
        UUID customerId = randomUuid();
        Customer customer = randomCustomer(customerId, CustomerStatus.ACTIVE);
        CustomerResponse response = randomCustomerResponse();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.suspendCustomer(customerId);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
    }

    @Test
    void shouldCloseCustomer() {
        // Arrange
        UUID customerId = randomUuid();
        Customer customer = randomCustomer(customerId, CustomerStatus.ACTIVE);
        CustomerResponse response = randomCustomerResponse();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.closeCustomer(customerId);

        // Assert
        assertThat(result).isSameAs(response);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.CLOSED);
    }

    @Test
    void shouldDeleteCustomer() {
        // Arrange
        UUID customerId = randomUuid();
        Customer customer = randomCustomer(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Act
        customerService.deleteCustomer(customerId);

        // Assert
        verify(customerRepository).delete(customer);
    }

    @Test
    void shouldRejectMissingCustomerActivation() {
        // Arrange
        UUID customerId = randomUuid();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.activateCustomer(customerId))
            .isInstanceOf(CustomerNotFoundException.class);
    }

}
