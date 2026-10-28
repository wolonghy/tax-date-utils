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

package com.wolonghy.taxdate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateRange}.
 */
@DisplayName("DateRange")
class DateRangeTest {

    // ==================== Construction ====================

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("valid range is created successfully")
        void validRange() {
            DateRange range = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            assertEquals(LocalDate.of(2026, 1, 1), range.start());
            assertEquals(LocalDate.of(2026, 12, 31), range.end());
        }

        @Test
        @DisplayName("single-day range (start == end) is valid")
        void singleDayRange() {
            LocalDate date = LocalDate.of(2026, 6, 15);
            DateRange range = new DateRange(date, date);
            assertEquals(date, range.start());
            assertEquals(date, range.end());
        }

        @Test
        @DisplayName("null start throws NullPointerException")
        void nullStart() {
            assertThrows(NullPointerException.class,
                    () -> new DateRange(null, LocalDate.of(2026, 12, 31)));
        }

        @Test
        @DisplayName("null end throws NullPointerException")
        void nullEnd() {
            assertThrows(NullPointerException.class,
                    () -> new DateRange(LocalDate.of(2026, 1, 1), null));
        }

        @Test
        @DisplayName("start after end throws IllegalArgumentException")
        void startAfterEnd() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DateRange(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));
        }
    }

    // ==================== daysBetween / monthsBetween ====================

    @Nested
    @DisplayName("Duration calculations")
    class Duration {

        @Test
        @DisplayName("daysBetween counts inclusively")
        void daysBetween() {
            // Jan 1 to Jan 31 = 31 days (inclusive)
            DateRange jan = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
            assertEquals(31, jan.daysBetween());
        }

        @Test
        @DisplayName("daysBetween for single-day range returns 1")
        void daysBetween_singleDay() {
            LocalDate date = LocalDate.of(2026, 6, 15);
            DateRange range = new DateRange(date, date);
            assertEquals(1, range.daysBetween());
        }

        @Test
        @DisplayName("monthsBetween counts inclusively")
        void monthsBetween() {
            // Jan 15 to Mar 10 = 3 months (Jan, Feb, Mar)
            DateRange range = new DateRange(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 10));
            assertEquals(3, range.monthsBetween());
        }

        @Test
        @DisplayName("monthsBetween for full year returns 12")
        void monthsBetween_fullYear() {
            DateRange range = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            assertEquals(12, range.monthsBetween());
        }
    }

    // ==================== contains ====================

    @Nested
    @DisplayName("contains()")
    class Contains {

        private final DateRange range = new DateRange(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31));

        @Test
        @DisplayName("date within range returns true")
        void withinRange() {
            assertTrue(range.contains(LocalDate.of(2026, 4, 15)));
        }

        @Test
        @DisplayName("start boundary is inclusive")
        void startBoundary() {
            assertTrue(range.contains(LocalDate.of(2026, 3, 1)));
        }

        @Test
        @DisplayName("end boundary is inclusive")
        void endBoundary() {
            assertTrue(range.contains(LocalDate.of(2026, 5, 31)));
        }

        @Test
        @DisplayName("date before range returns false")
        void beforeRange() {
            assertFalse(range.contains(LocalDate.of(2026, 2, 28)));
        }

        @Test
        @DisplayName("date after range returns false")
        void afterRange() {
            assertFalse(range.contains(LocalDate.of(2026, 6, 1)));
        }

        @Test
        @DisplayName("null date throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> range.contains(null));
        }
    }

    // ==================== overlaps ====================

    @Nested
    @DisplayName("overlaps()")
    class Overlaps {

        @Test
        @DisplayName("overlapping ranges return true")
        void overlapping() {
            DateRange r1 = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            DateRange r2 = new DateRange(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30));
            assertTrue(r1.overlaps(r2));
            assertTrue(r2.overlaps(r1));
        }

        @Test
        @DisplayName("adjacent but non-overlapping ranges return false")
        void adjacent() {
            DateRange r1 = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
            DateRange r2 = new DateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30));
            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("sharing a single boundary date counts as overlap")
        void sharedBoundary() {
            DateRange r1 = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
            DateRange r2 = new DateRange(LocalDate.of(2026, 3, 31), LocalDate.of(2026, 6, 30));
            assertTrue(r1.overlaps(r2));
        }

        @Test
        @DisplayName("completely disjoint ranges return false")
        void disjoint() {
            DateRange r1 = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
            DateRange r2 = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31));
            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("contained range overlaps")
        void contained() {
            DateRange outer = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            DateRange inner = new DateRange(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31));
            assertTrue(outer.overlaps(inner));
            assertTrue(inner.overlaps(outer));
        }

        @Test
        @DisplayName("null other throws NullPointerException")
        void nullOther() {
            DateRange range = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            assertThrows(NullPointerException.class, () -> range.overlaps(null));
        }
    }

    // ==================== Formatting ====================

    @Nested
    @DisplayName("Formatting")
    class Formatting {

        @Test
        @DisplayName("formatStart and formatEnd use the given formatter")
        void formatWithFormatter() {
            DateRange range = new DateRange(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 20));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            assertEquals("2026/01/15", range.formatStart(fmt));
            assertEquals("2026/03/20", range.formatEnd(fmt));
        }
    }
}
