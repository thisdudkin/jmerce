/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.exception.DomainValidationException;

import java.time.Instant;

public record AuditTimestamps(Instant createdAt, Instant updatedAt) {

    public AuditTimestamps {
        if (createdAt == null) {
            throw new DomainValidationException("Created timestamp can not be null");
        }
        if (updatedAt == null) {
            throw new DomainValidationException("Updated timestamp can not be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new DomainValidationException("Updated timestamp must not be before created timestamp");
        }
    }

    public static AuditTimestamps created(Instant timestamp) {
        return new AuditTimestamps(timestamp, timestamp);
    }

    public AuditTimestamps updated(Instant timestamp) {
        if (timestamp.isBefore(updatedAt)) {
            throw new DomainValidationException("Updated timestamp must not move backwards");
        }
        return new AuditTimestamps(createdAt, timestamp);
    }

}
