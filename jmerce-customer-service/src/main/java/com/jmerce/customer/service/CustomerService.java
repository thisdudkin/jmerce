/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service;

import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerDetailsResponse;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.rest.dto.CustomerUpdateRequest;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerCreateRequest request);

    CustomerDetailsResponse getCustomer(UUID customerId);

    CustomerResponse updateCustomer(UUID customerId, CustomerUpdateRequest request);

    void deleteCustomer(UUID customerId);

}
