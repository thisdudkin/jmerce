/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.mapper;

import com.jmerce.customer.entity.Customer;
import com.jmerce.customer.rest.dto.CustomerCreateRequest;
import com.jmerce.customer.rest.dto.CustomerDetailsResponse;
import com.jmerce.customer.rest.dto.CustomerResponse;
import com.jmerce.customer.rest.dto.CustomerUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
    componentModel = SPRING,
    injectionStrategy = CONSTRUCTOR,
    uses = AddressMapper.class
)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void update(@MappingTarget Customer customer, CustomerUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    Customer toEntity(CustomerCreateRequest request);

    CustomerResponse toResponse(Customer customer);

    CustomerDetailsResponse toDetailsResponse(Customer customer);

}
