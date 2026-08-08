/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain.model.value;

import com.jmerce.customer.domain.RandomTestData;
import com.jmerce.customer.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditTimestampsTest {

    @Test
    void shouldCreateInitialTimestamps() {
        // Arrange
        Instant timestamp = RandomTestData.instant();

        // Act
        AuditTimestamps timestamps = AuditTimestamps.created(timestamp);

        // Assert
        assertThat(timestamps.createdAt()).isEqualTo(timestamp);
        assertThat(timestamps.updatedAt()).isEqualTo(timestamp);
    }

    @Test
    void shouldAdvanceUpdatedTimestampAndKeepCreatedTimestamp() {
        // Arrange
        Instant createdAt = RandomTestData.instant();
        Instant updatedAt = RandomTestData.after(createdAt);
        AuditTimestamps timestamps = AuditTimestamps.created(createdAt);

        // Act
        AuditTimestamps updated = timestamps.updated(updatedAt);

        // Assert
        assertThat(updated.createdAt()).isEqualTo(createdAt);
        assertThat(updated.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldAllowIdempotentTimestampUpdate() {
        // Arrange
        Instant timestamp = RandomTestData.instant();
        AuditTimestamps timestamps = AuditTimestamps.created(timestamp);

        // Act
        AuditTimestamps updated = timestamps.updated(timestamp);

        // Assert
        assertThat(updated).isEqualTo(timestamps);
    }

    @Test
    void shouldRejectNullTimestamps() {
        // Arrange
        Instant timestamp = RandomTestData.instant();

        // Act & Assert
        assertThatThrownBy(() -> new AuditTimestamps(null, timestamp))
            .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new AuditTimestamps(timestamp, null))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectUpdatedTimestampBeforeCreatedTimestamp() {
        // Arrange
        Instant createdAt = RandomTestData.instant();
        Instant earlierTimestamp = RandomTestData.before(createdAt);

        // Act & Assert
        assertThatThrownBy(() -> new AuditTimestamps(createdAt, earlierTimestamp))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectMovingUpdatedTimestampBackwards() {
        // Arrange
        Instant createdAt = RandomTestData.instant();
        Instant updatedAt = RandomTestData.after(createdAt);
        Instant earlierTimestamp = RandomTestData.before(updatedAt);
        AuditTimestamps timestamps = new AuditTimestamps(createdAt, updatedAt);

        // Act & Assert
        assertThatThrownBy(() -> timestamps.updated(earlierTimestamp))
            .isInstanceOf(DomainValidationException.class);
    }

}
