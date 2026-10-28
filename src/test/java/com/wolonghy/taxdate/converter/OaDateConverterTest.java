/*
 * Copyright 2026 Beijing Wolong Huiyan Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wolonghy.taxdate.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OaDateConverter}.
 */
@DisplayName("OaDateConverter")
class OaDateConverterTest {

    // ==================== toOaDate(String) ====================

    @Nested
    @DisplayName("toOaDate(String)")
    class ToOaDateFromString {

        @Test
        @DisplayName("converts yyyy/MM/dd to OA Date format")
        void validConversion() {
            String result = OaDateConverter.toOaDate("2026/01/15");
            assertNotNull(result);
            assertTrue(result.startsWith("/OADate("));
            assertTrue(result.endsWith(")/"));
        }

        @Test
        @DisplayName("epoch date 1899/12/30 returns /OADate(0)/")
        void epochDate() {
            assertEquals("/OADate(0)/", OaDateConverter.toOaDate("1899/12/30"));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> OaDateConverter.toOaDate((String) null));
        }

        @Test
        @DisplayName("blank input throws IllegalArgumentException")
        void blankInput() {
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.toOaDate(""));
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.toOaDate("   "));
        }

        @Test
        @DisplayName("invalid format throws IllegalArgumentException")
        void invalidFormat() {
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.toOaDate("2026-01-15")); // wrong format (dash, not slash)
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.toOaDate("not-a-date"));
        }
    }

    // ==================== toOaDate(LocalDate) ====================

    @Nested
    @DisplayName("toOaDate(LocalDate)")
    class ToOaDateFromLocalDate {

        @Test
        @DisplayName("converts LocalDate to OA Date format")
        void validConversion() {
            String result = OaDateConverter.toOaDate(LocalDate.of(2026, 1, 15));
            assertNotNull(result);
            assertTrue(result.startsWith("/OADate("));
        }

        @Test
        @DisplayName("epoch date returns /OADate(0)/")
        void epochDate() {
            assertEquals("/OADate(0)/", OaDateConverter.toOaDate(LocalDate.of(1899, 12, 30)));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> OaDateConverter.toOaDate((LocalDate) null));
        }
    }

    // ==================== fromOaDate ====================

    @Nested
    @DisplayName("fromOaDate(String)")
    class FromOaDate {

        @Test
        @DisplayName("converts OA Date to yyyy-MM-dd string")
        void validConversion() {
            String oaDate = OaDateConverter.toOaDate("2026/01/15");
            assertEquals("2026-01-15", OaDateConverter.fromOaDate(oaDate));
        }

        @Test
        @DisplayName("round-trip: toOaDate → fromOaDate preserves date")
        void roundTrip() {
            String original = "2026/06/30";
            String oaDate = OaDateConverter.toOaDate(original);
            assertEquals(original.replace('/', '-'), OaDateConverter.fromOaDate(oaDate));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> OaDateConverter.fromOaDate(null));
        }

        @Test
        @DisplayName("invalid OA Date format throws IllegalArgumentException")
        void invalidFormat() {
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.fromOaDate("not-an-oa-date"));
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.fromOaDate("/OADate("));       // missing closing
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.fromOaDate("/OADate(abc)/"));  // non-numeric
        }
    }

    // ==================== fromOaDateToDate ====================

    @Nested
    @DisplayName("fromOaDateToDate(String)")
    class FromOaDateToDate {

        @Test
        @DisplayName("converts OA Date to LocalDate")
        void validConversion() {
            LocalDate expected = LocalDate.of(2026, 1, 15);
            String oaDate = OaDateConverter.toOaDate(expected);
            assertEquals(expected, OaDateConverter.fromOaDateToDate(oaDate));
        }

        @Test
        @DisplayName("round-trip: toOaDate(LocalDate) → fromOaDateToDate preserves date")
        void roundTrip() {
            LocalDate original = LocalDate.of(2024, 2, 29); // leap year
            String oaDate = OaDateConverter.toOaDate(original);
            assertEquals(original, OaDateConverter.fromOaDateToDate(oaDate));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> OaDateConverter.fromOaDateToDate(null));
        }

        @Test
        @DisplayName("malformed input throws IllegalArgumentException")
        void malformedInput() {
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.fromOaDateToDate("garbage"));
            assertThrows(IllegalArgumentException.class, () -> OaDateConverter.fromOaDateToDate("/OADate("));
        }
    }

    // ==================== Known values ====================

    @Nested
    @DisplayName("Known OA Date values")
    class KnownValues {

        @Test
        @DisplayName("2000/01/01 = /OADate(36526)/")
        void y2k() {
            // Days from 1899-12-30 to 2000-01-01
            long expectedDays = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.of(1899, 12, 30), LocalDate.of(2000, 1, 1));
            assertEquals("/OADate(" + expectedDays + ")/",
                    OaDateConverter.toOaDate("2000/01/01"));
        }

        @Test
        @DisplayName("consistent conversion between String and LocalDate overloads")
        void consistentOverloads() {
            LocalDate date = LocalDate.of(2026, 7, 4);
            String fromString = OaDateConverter.toOaDate("2026/07/04");
            String fromDate = OaDateConverter.toOaDate(date);
            assertEquals(fromString, fromDate);
        }

        @Test
        @DisplayName("dates before epoch produce negative OA Date values")
        void beforeEpoch() {
            LocalDate beforeEpoch = LocalDate.of(1899, 12, 29);
            String result = OaDateConverter.toOaDate(beforeEpoch);
            assertEquals("/OADate(-1)/", result);
            
            LocalDate farBefore = LocalDate.of(1899, 1, 1);
            String farResult = OaDateConverter.toOaDate(farBefore);
            assertTrue(farResult.startsWith("/OADate(-"));
        }

        @Test
        @DisplayName("negative OA Date values convert back correctly")
        void negativeOaDate() {
            String negativeOaDate = "/OADate(-1)/";
            assertEquals("1899-12-29", OaDateConverter.fromOaDate(negativeOaDate));
            assertEquals(LocalDate.of(1899, 12, 29), OaDateConverter.fromOaDateToDate(negativeOaDate));
        }

        @Test
        @DisplayName("round-trip with negative OA Date")
        void roundTripNegative() {
            LocalDate original = LocalDate.of(1890, 5, 15);
            String oaDate = OaDateConverter.toOaDate(original);
            assertEquals(original, OaDateConverter.fromOaDateToDate(oaDate));
        }
    }
}
