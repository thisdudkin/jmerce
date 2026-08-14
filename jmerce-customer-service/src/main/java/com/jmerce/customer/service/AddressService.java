/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.service;

import com.jmerce.customer.rest.dto.AddressCreateRequest;
import com.jmerce.customer.rest.dto.AddressResponse;
import com.jmerce.customer.rest.dto.AddressUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressResponse> listAddresses(UUID customerId);

    AddressResponse createAddress(UUID customerId, AddressCreateRequest request);

    AddressResponse getAddress(UUID customerId, UUID addressId);

    AddressResponse updateAddress(UUID customerId, UUID addressId, AddressUpdateRequest request);

    void deleteAddress(UUID customerId, UUID addressId);

    AddressResponse makeDefault(UUID customerId, UUID addressId);

}
