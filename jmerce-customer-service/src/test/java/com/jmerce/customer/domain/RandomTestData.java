/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Aliaksandr Dudkin
 * SPDX-License-Identifier: Apache-2.0
 */

package com.jmerce.customer.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomTestData {

    private RandomTestData() {
    }

    public static UUID uuid() {
        return UUID.randomUUID();
    }

    public static Instant instant() {
        long epochSecond = ThreadLocalRandom.current()
            .nextLong(1, Instant.now().getEpochSecond());
        return Instant.ofEpochSecond(epochSecond);
    }

    public static Instant after(Instant timestamp) {
        return timestamp.plusSeconds(ThreadLocalRandom.current().nextLong(1, 86_401));
    }

    public static Instant before(Instant timestamp) {
        return timestamp.minusSeconds(ThreadLocalRandom.current().nextLong(1, 86_401));
    }

    public static String alphabetic(int length) {
        StringBuilder value = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < length; index++) {
            value.append((char) random.nextInt('a', 'z' + 1));
        }
        return value.toString();
    }

    public static String alphabetic() {
        return alphabetic(ThreadLocalRandom.current().nextInt(5, 21));
    }

    public static String uppercaseAlphabetic(int length) {
        return alphabetic(length).toUpperCase(Locale.ROOT);
    }

    public static String whitespace() {
        return " ".repeat(ThreadLocalRandom.current().nextInt(1, 6));
    }

    public static String phoneNumber() {
        return phoneNumber(ThreadLocalRandom.current().nextInt(8, 16));
    }

    public static String phoneNumber(int digitCount) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder value = new StringBuilder(digitCount + 1);
        value.append('+');
        value.append(random.nextInt(1, 10));
        for (int index = 1; index < digitCount; index++) {
            value.append(random.nextInt(10));
        }
        return value.toString();
    }

}
