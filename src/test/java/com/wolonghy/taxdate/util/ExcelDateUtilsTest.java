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

package com.wolonghy.taxdate.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExcelDateUtils")
class ExcelDateUtilsTest {

    @Nested
    @DisplayName("toLocalDate(double)")
    class ToLocalDateDouble {

        @Test
        @DisplayName("converts known Excel date value")
        void knownValue() {
            // 2026-01-15 is 46037 days after 1899-12-30
            LocalDate result = ExcelDateUtils.toLocalDate(46037.0);
            assertEquals(LocalDate.of(2026, 1, 15), result);
        }

        @Test
        @DisplayName("epoch value 0 returns 1899-12-30")
        void epoch() {
            assertEquals(LocalDate.of(1899, 12, 30), ExcelDateUtils.toLocalDate(0.0));
        }

        @Test
        @DisplayName("value 1 returns 1899-12-31")
        void one() {
            assertEquals(LocalDate.of(1899, 12, 31), ExcelDateUtils.toLocalDate(1.0));
        }

        @Test
        @DisplayName("NaN throws IllegalArgumentException")
        void nan() {
            assertThrows(IllegalArgumentException.class, () -> ExcelDateUtils.toLocalDate(Double.NaN));
        }

        @Test
        @DisplayName("negative value returns date before epoch")
        void negative() {
            assertEquals(LocalDate.of(1899, 12, 29), ExcelDateUtils.toLocalDate(-1.0));
        }

        @Test
        @DisplayName("fractional value truncates to integer days")
        void fractional() {
            LocalDate result = ExcelDateUtils.toLocalDate(46037.7);
            assertEquals(LocalDate.of(2026, 1, 15), result);
        }
    }

    @Nested
    @DisplayName("toLocalDate(String)")
    class ToLocalDateString {

        @Test
        @DisplayName("converts valid numeric string")
        void validString() {
            assertEquals(LocalDate.of(2026, 1, 15), ExcelDateUtils.toLocalDate("46037"));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> ExcelDateUtils.toLocalDate((String) null));
        }

        @Test
        @DisplayName("blank throws IllegalArgumentException")
        void blankInput() {
            assertThrows(IllegalArgumentException.class, () -> ExcelDateUtils.toLocalDate("  "));
        }

        @Test
        @DisplayName("non-numeric throws IllegalArgumentException")
        void nonNumeric() {
            assertThrows(IllegalArgumentException.class, () -> ExcelDateUtils.toLocalDate("abc"));
        }
    }

    @Nested
    @DisplayName("format(double, String)")
    class Format {

        @Test
        @DisplayName("formats with custom pattern")
        void customPattern() {
            String result = ExcelDateUtils.format(46037.0, "yyyy/MM/dd");
            assertEquals("2026/01/15", result);
        }

        @Test
        @DisplayName("NaN throws IllegalArgumentException")
        void nanReturnsNull() {
            assertThrows(IllegalArgumentException.class, () -> ExcelDateUtils.format(Double.NaN, "yyyy-MM-dd"));
        }

        @Test
        @DisplayName("null pattern throws NullPointerException")
        void nullPattern() {
            assertThrows(NullPointerException.class, () -> ExcelDateUtils.format(46037.0, null));
        }
    }

    @Nested
    @DisplayName("formatDate / formatDateSlash")
    class FormatPresets {

        @Test
        @DisplayName("formatDate returns yyyy-MM-dd")
        void formatDate() {
            assertEquals("2026-01-15", ExcelDateUtils.formatDate(46037.0));
        }

        @Test
        @DisplayName("formatDateSlash returns yyyy/MM/dd")
        void formatDateSlash() {
            assertEquals("2026/01/15", ExcelDateUtils.formatDateSlash(46037.0));
        }

        @Test
        @DisplayName("formatDate with NaN throws IllegalArgumentException")
        void formatDateNaN() {
            assertThrows(IllegalArgumentException.class, () -> ExcelDateUtils.formatDate(Double.NaN));
        }

        @Test
        @DisplayName("formatDateSlash with NaN throws IllegalArgumentException")
        void formatDateSlashNaN() {
            assertThrows(IllegalArgumentException.class, () -> ExcelDateUtils.formatDateSlash(Double.NaN));
        }
    }

    @Nested
    @DisplayName("isExcelDate")
    class IsExcelDate {

        @Test
        @DisplayName("valid date number returns true")
        void validDate() {
            assertTrue(ExcelDateUtils.isExcelDate(1.0));
            assertTrue(ExcelDateUtils.isExcelDate(46037.0));
        }

        @Test
        @DisplayName("max boundary returns true")
        void maxBoundary() {
            assertTrue(ExcelDateUtils.isExcelDate(2958465.0));
        }

        @Test
        @DisplayName("zero returns false")
        void zero() {
            assertFalse(ExcelDateUtils.isExcelDate(0.0));
        }

        @Test
        @DisplayName("negative returns false")
        void negative() {
            assertFalse(ExcelDateUtils.isExcelDate(-1.0));
        }

        @Test
        @DisplayName("NaN returns false")
        void nan() {
            assertFalse(ExcelDateUtils.isExcelDate(Double.NaN));
        }

        @Test
        @DisplayName("above max returns false")
        void aboveMax() {
            assertFalse(ExcelDateUtils.isExcelDate(2958466.0));
        }
    }

    @Nested
    @DisplayName("toExcelNumeric")
    class ToExcelNumeric {

        @Test
        @DisplayName("converts known date")
        void knownDate() {
            assertEquals(46037L, ExcelDateUtils.toExcelNumeric(LocalDate.of(2026, 1, 15)));
        }

        @Test
        @DisplayName("epoch date returns 0")
        void epochDate() {
            assertEquals(0L, ExcelDateUtils.toExcelNumeric(LocalDate.of(1899, 12, 30)));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> ExcelDateUtils.toExcelNumeric(null));
        }

        @Test
        @DisplayName("round-trip consistency")
        void roundTrip() {
            LocalDate original = LocalDate.of(2026, 6, 15);
            long numeric = ExcelDateUtils.toExcelNumeric(original);
            LocalDate restored = ExcelDateUtils.toLocalDate(numeric);
            assertEquals(original, restored);
        }
    }
}
