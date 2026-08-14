/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.rest.contoller;

import com.jmerce.customer.enums.AddressPurpose;
import com.jmerce.customer.rest.dto.AddressCreateRequest;
import com.jmerce.customer.rest.dto.AddressResponse;
import com.jmerce.customer.rest.dto.AddressUpdateRequest;
import com.jmerce.customer.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static com.jmerce.customer.util.RandomTestData.randomAddressCreateRequest;
import static com.jmerce.customer.util.RandomTestData.randomAddressResponse;
import static com.jmerce.customer.util.RandomTestData.randomAddressUpdateRequest;
import static com.jmerce.customer.util.RandomTestData.randomCountryCode;
import static com.jmerce.customer.util.RandomTestData.randomString;
import static com.jmerce.customer.util.RandomTestData.randomUuid;
import static com.jmerce.customer.rest.api.AddressesApi.PATH_CREATE_ADDRESS;
import static com.jmerce.customer.rest.api.AddressesApi.PATH_DELETE_ADDRESS;
import static com.jmerce.customer.rest.api.AddressesApi.PATH_GET_ADDRESS;
import static com.jmerce.customer.rest.api.AddressesApi.PATH_LIST_ADDRESSES;
import static com.jmerce.customer.rest.api.AddressesApi.PATH_MAKE_DEFAULT;
import static com.jmerce.customer.rest.api.AddressesApi.PATH_UPDATE_ADDRESS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressRestController.class)
class AddressRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @Test
    void shouldCreateAddress() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        AddressCreateRequest request = randomAddressCreateRequest();
        AddressResponse response = randomAddressResponse(customerId);
        when(addressService.createAddress(customerId, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(PATH_CREATE_ADDRESS, customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string(
                LOCATION,
                "http://localhost/customers/" + customerId + "/addresses/" + response.getId()
            ))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(addressService).createAddress(customerId, request);
    }

    @Test
    void shouldRejectInvalidAddressCreation() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        AddressCreateRequest request = new AddressCreateRequest(
            AddressPurpose.SHIPPING,
            "",
            randomString(),
            randomString(),
            randomCountryCode()
        );

        // Act & Assert
        mockMvc.perform(post(PATH_CREATE_ADDRESS, customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(addressService);
    }

    @Test
    void shouldListAddresses() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        List<AddressResponse> response = List.of(
            randomAddressResponse(customerId),
            randomAddressResponse(customerId)
        );
        when(addressService.listAddresses(customerId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(PATH_LIST_ADDRESSES, customerId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(addressService).listAddresses(customerId);
    }

    @Test
    void shouldGetAddress() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        AddressResponse response = randomAddressResponse(customerId);
        when(addressService.getAddress(customerId, response.getId())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(PATH_GET_ADDRESS, customerId, response.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(addressService).getAddress(customerId, response.getId());
    }

    @Test
    void shouldUpdateAddress() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        UUID addressId = randomUuid();
        AddressUpdateRequest request = randomAddressUpdateRequest();
        AddressResponse response = randomAddressResponse(customerId);
        when(addressService.updateAddress(customerId, addressId, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(PATH_UPDATE_ADDRESS, customerId, addressId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(addressService).updateAddress(customerId, addressId, request);
    }

    @Test
    void shouldMakeAddressDefault() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        UUID addressId = randomUuid();
        AddressResponse response = randomAddressResponse(customerId);
        when(addressService.makeDefault(customerId, addressId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(PATH_MAKE_DEFAULT, customerId, addressId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(addressService).makeDefault(customerId, addressId);
    }

    @Test
    void shouldDeleteAddress() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        UUID addressId = randomUuid();

        // Act & Assert
        mockMvc.perform(delete(PATH_DELETE_ADDRESS, customerId, addressId))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        verify(addressService).deleteAddress(customerId, addressId);
    }

}
