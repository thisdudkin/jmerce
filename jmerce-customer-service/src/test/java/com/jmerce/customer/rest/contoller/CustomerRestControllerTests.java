/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.rest.contoller;

import com.jmerce.customer.exception.CustomerAlreadyExistsException;
import com.jmerce.customer.exception.CustomerNotFoundException;
import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerDetailsResponse;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.rest.dto.CustomerUpdateRequest;
import com.jmerce.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static com.jmerce.customer.util.RandomTestData.randomCustomerCreateRequest;
import static com.jmerce.customer.util.RandomTestData.randomCustomerDetailsResponse;
import static com.jmerce.customer.util.RandomTestData.randomCustomerResponse;
import static com.jmerce.customer.util.RandomTestData.randomCustomerUpdateRequest;
import static com.jmerce.customer.util.RandomTestData.randomString;
import static com.jmerce.customer.util.RandomTestData.randomUuid;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_ACTIVATE_CUSTOMER;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_CLOSE_CUSTOMER;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_CREATE_CUSTOMER;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_DELETE_CUSTOMER;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_GET_CUSTOMER;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_SUSPEND_CUSTOMER;
import static com.jmerce.customer.rest.api.CustomersApi.PATH_UPDATE_CUSTOMER;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerRestController.class)
class CustomerRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() throws Exception {
        // Arrange
        CustomerCreateRequest request = randomCustomerCreateRequest();
        CustomerResponse response = randomCustomerResponse();
        when(customerService.createCustomer(request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(PATH_CREATE_CUSTOMER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string(LOCATION, "http://localhost/customers/" + response.getId()))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(customerService).createCustomer(request);
    }

    @Test
    void shouldReturnConflictWhenCustomerAlreadyExists() throws Exception {
        // Arrange
        CustomerCreateRequest request = randomCustomerCreateRequest();
        var exception = new CustomerAlreadyExistsException(request.getUserId());
        when(customerService.createCustomer(request)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(post(PATH_CREATE_CUSTOMER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        verify(customerService).createCustomer(request);
    }

    @Test
    void shouldRejectInvalidCustomerCreation() throws Exception {
        // Arrange
        CustomerCreateRequest request = new CustomerCreateRequest(randomUuid(), "", randomString());

        // Act & Assert
        mockMvc.perform(post(PATH_CREATE_CUSTOMER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(customerService);
    }

    @Test
    void shouldRejectMalformedCustomerCreation() throws Exception {
        // Act & Assert
        mockMvc.perform(post(PATH_CREATE_CUSTOMER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(customerService);
    }

    @Test
    void shouldGetCustomer() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        CustomerDetailsResponse response = randomCustomerDetailsResponse();
        when(customerService.getCustomer(customerId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(PATH_GET_CUSTOMER, customerId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(customerService).getCustomer(customerId);
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        var exception = new CustomerNotFoundException(customerId);
        when(customerService.getCustomer(customerId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get(PATH_GET_CUSTOMER, customerId))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        verify(customerService).getCustomer(customerId);
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        when(customerService.getCustomer(customerId)).thenThrow(new IllegalStateException("Sensitive details"));

        // Act & Assert
        mockMvc.perform(get(PATH_GET_CUSTOMER, customerId))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").doesNotExist());
        verify(customerService).getCustomer(customerId);
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        CustomerUpdateRequest request = randomCustomerUpdateRequest();
        CustomerResponse response = randomCustomerResponse();
        when(customerService.updateCustomer(customerId, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(PATH_UPDATE_CUSTOMER, customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(customerService).updateCustomer(customerId, request);
    }

    @Test
    void shouldActivateCustomer() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        CustomerResponse response = randomCustomerResponse();
        when(customerService.activateCustomer(customerId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(PATH_ACTIVATE_CUSTOMER, customerId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(customerService).activateCustomer(customerId);
    }

    @Test
    void shouldSuspendCustomer() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        CustomerResponse response = randomCustomerResponse();
        when(customerService.suspendCustomer(customerId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(PATH_SUSPEND_CUSTOMER, customerId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(customerService).suspendCustomer(customerId);
    }

    @Test
    void shouldCloseCustomer() throws Exception {
        // Arrange
        UUID customerId = randomUuid();
        CustomerResponse response = randomCustomerResponse();
        when(customerService.closeCustomer(customerId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(PATH_CLOSE_CUSTOMER, customerId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response), JsonCompareMode.STRICT));
        verify(customerService).closeCustomer(customerId);
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        // Arrange
        UUID customerId = randomUuid();

        // Act & Assert
        mockMvc.perform(delete(PATH_DELETE_CUSTOMER, customerId))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        verify(customerService).deleteCustomer(customerId);
    }

}
