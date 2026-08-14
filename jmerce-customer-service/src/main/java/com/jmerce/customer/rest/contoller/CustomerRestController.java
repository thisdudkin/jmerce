/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.rest.contoller;

import com.jmerce.customer.rest.api.CustomersApi;
import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerDetailsResponse;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.rest.dto.CustomerUpdateRequest;
import com.jmerce.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CustomerRestController implements CustomersApi {

    private final CustomerService customerService;

    @Override
    public ResponseEntity<CustomerResponse> activateCustomer(UUID customerId) {
        var response = customerService.activateCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerResponse> closeCustomer(UUID customerId) {
        var response = customerService.closeCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CustomerCreateRequest request) {
        var response = customerService.createCustomer(request);
        var location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{customerId}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CustomerDetailsResponse> getCustomer(UUID customerId) {
        var response = customerService.getCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerResponse> suspendCustomer(UUID customerId) {
        var response = customerService.suspendCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(UUID customerId, CustomerUpdateRequest request) {
        var response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

}
