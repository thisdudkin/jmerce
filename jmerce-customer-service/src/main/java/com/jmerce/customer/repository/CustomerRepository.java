/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.repository;

import com.jmerce.customer.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByUserId(UUID userId);

    @EntityGraph(attributePaths = "addresses")
    Optional<Customer> findWithAddressesById(UUID customerId);

    @Query("""
        SELECT c
        FROM Customer c
        WHERE c.id = :customerId
        """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Customer> findByIdForUpdate(UUID customerId);

}
