/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.rest.contoller;

import com.jmerce.customer.rest.api.AddressesApi;
import com.jmerce.customer.rest.dto.AddressCreateRequest;
import com.jmerce.customer.rest.dto.AddressResponse;
import com.jmerce.customer.rest.dto.AddressUpdateRequest;
import com.jmerce.customer.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AddressRestController implements AddressesApi {

    private final AddressService addressService;

    @Override
    public ResponseEntity<AddressResponse> createAddress(UUID customerId, AddressCreateRequest request) {
        var response = addressService.createAddress(customerId, request);
        var location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{addressId}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteAddress(UUID customerId, UUID addressId) {
        addressService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AddressResponse> getAddress(UUID customerId, UUID addressId) {
        var response = addressService.getAddress(customerId, addressId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<AddressResponse>> listAddresses(UUID customerId) {
        var response = addressService.listAddresses(customerId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AddressResponse> makeDefault(UUID customerId, UUID addressId) {
        var response = addressService.makeDefault(customerId, addressId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AddressResponse> updateAddress(UUID customerId, UUID addressId, AddressUpdateRequest request) {
        var response = addressService.updateAddress(customerId, addressId, request);
        return ResponseEntity.ok(response);
    }

}
