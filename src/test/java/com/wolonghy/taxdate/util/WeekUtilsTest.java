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

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeekUtils")
class WeekUtilsTest {

    @Nested
    @DisplayName("getMondayOfWeek")
    class GetMondayOfWeek {

        @Test
        @DisplayName("Monday returns same date")
        void monday() {
            LocalDate monday = LocalDate.of(2026, 10, 26); // a Monday
            assertEquals(monday, WeekUtils.getMondayOfWeek(monday));
        }

        @Test
        @DisplayName("Wednesday returns Monday")
        void wednesday() {
            LocalDate wednesday = LocalDate.of(2026, 10, 28);
            LocalDate expected = LocalDate.of(2026, 10, 26);
            assertEquals(expected, WeekUtils.getMondayOfWeek(wednesday));
        }

        @Test
        @DisplayName("Sunday returns previous Monday")
        void sunday() {
            LocalDate sunday = LocalDate.of(2026, 11, 1);
            LocalDate expected = LocalDate.of(2026, 10, 26);
            assertEquals(expected, WeekUtils.getMondayOfWeek(sunday));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> WeekUtils.getMondayOfWeek(null));
        }
    }

    @Nested
    @DisplayName("getSundayOfWeek")
    class GetSundayOfWeek {

        @Test
        @DisplayName("Sunday returns same date")
        void sunday() {
            LocalDate sunday = LocalDate.of(2026, 11, 1);
            assertEquals(sunday, WeekUtils.getSundayOfWeek(sunday));
        }

        @Test
        @DisplayName("Monday returns next Sunday")
        void monday() {
            LocalDate monday = LocalDate.of(2026, 10, 26);
            LocalDate expected = LocalDate.of(2026, 11, 1);
            assertEquals(expected, WeekUtils.getSundayOfWeek(monday));
        }

        @Test
        @DisplayName("Saturday returns next day")
        void saturday() {
            LocalDate saturday = LocalDate.of(2026, 10, 31);
            LocalDate expected = LocalDate.of(2026, 11, 1);
            assertEquals(expected, WeekUtils.getSundayOfWeek(saturday));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> WeekUtils.getSundayOfWeek(null));
        }
    }

    @Nested
    @DisplayName("getWeekRange")
    class GetWeekRange {

        @Test
        @DisplayName("returns Monday and Sunday")
        void weekRange() {
            LocalDate wednesday = LocalDate.of(2026, 10, 28);
            LocalDate[] range = WeekUtils.getWeekRange(wednesday);
            assertEquals(2, range.length);
            assertEquals(LocalDate.of(2026, 10, 26), range[0]); // Monday
            assertEquals(LocalDate.of(2026, 11, 1), range[1]);   // Sunday
        }

        @Test
        @DisplayName("Monday to Sunday span is 6 days")
        void span() {
            LocalDate[] range = WeekUtils.getWeekRange(LocalDate.of(2026, 10, 28));
            assertEquals(6, range[1].toEpochDay() - range[0].toEpochDay());
        }
    }

    @Nested
    @DisplayName("getMondayOfCurrentWeek / getSundayOfCurrentWeek")
    class CurrentWeek {

        @Test
        @DisplayName("Monday of current week is a Monday")
        void mondayOfCurrentWeek() {
            LocalDate monday = WeekUtils.getMondayOfCurrentWeek();
            assertEquals(DayOfWeek.MONDAY, monday.getDayOfWeek());
        }

        @Test
        @DisplayName("Sunday of current week is a Sunday")
        void sundayOfCurrentWeek() {
            LocalDate sunday = WeekUtils.getSundayOfCurrentWeek();
            assertEquals(DayOfWeek.SUNDAY, sunday.getDayOfWeek());
        }

        @Test
        @DisplayName("Monday is before or equal to Sunday")
        void mondayBeforeSunday() {
            LocalDate monday = WeekUtils.getMondayOfCurrentWeek();
            LocalDate sunday = WeekUtils.getSundayOfCurrentWeek();
            assertFalse(monday.isAfter(sunday));
        }
    }

    @Nested
    @DisplayName("yesterday")
    class Yesterday {

        @Test
        @DisplayName("returns today minus one day")
        void yesterdayIsTodayMinusOne() {
            assertEquals(LocalDate.now().minusDays(1), WeekUtils.yesterday());
        }
    }

    @Nested
    @DisplayName("daysAgo")
    class DaysAgo {

        @Test
        @DisplayName("0 returns today")
        void zero() {
            assertEquals(LocalDate.now(), WeekUtils.daysAgo(0));
        }

        @Test
        @DisplayName("positive returns past date")
        void positive() {
            assertEquals(LocalDate.now().minusDays(5), WeekUtils.daysAgo(5));
        }

        @Test
        @DisplayName("negative throws IllegalArgumentException")
        void negative() {
            assertThrows(IllegalArgumentException.class, () -> WeekUtils.daysAgo(-1));
        }
    }

    @Nested
    @DisplayName("daysLater")
    class DaysLater {

        @Test
        @DisplayName("0 returns today")
        void zero() {
            assertEquals(LocalDate.now(), WeekUtils.daysLater(0));
        }

        @Test
        @DisplayName("positive returns future date")
        void positive() {
            assertEquals(LocalDate.now().plusDays(5), WeekUtils.daysLater(5));
        }

        @Test
        @DisplayName("negative throws IllegalArgumentException")
        void negative() {
            assertThrows(IllegalArgumentException.class, () -> WeekUtils.daysLater(-1));
        }
    }

    @Nested
    @DisplayName("getRecentDaysAsDates")
    class GetRecentDaysAsDates {

        @Test
        @DisplayName("returns N days including today")
        void positiveCount() {
            var result = WeekUtils.getRecentDaysAsDates(3);
            assertEquals(3, result.size());
            assertEquals(LocalDate.now(), result.get(0));
            assertEquals(LocalDate.now().minusDays(1), result.get(1));
            assertEquals(LocalDate.now().minusDays(2), result.get(2));
        }

        @Test
        @DisplayName("zero returns empty list")
        void zero() {
            assertTrue(WeekUtils.getRecentDaysAsDates(0).isEmpty());
        }

        @Test
        @DisplayName("negative returns empty list")
        void negative() {
            assertTrue(WeekUtils.getRecentDaysAsDates(-1).isEmpty());
        }
    }

    @Nested
    @DisplayName("getWeekNumber")
    class GetWeekNumber {

        @Test
        @DisplayName("known ISO week number")
        void knownWeek() {
            // 2026-01-01 is in ISO week 1
            assertEquals(1, WeekUtils.getWeekNumber(LocalDate.of(2026, 1, 1)));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> WeekUtils.getWeekNumber(null));
        }
    }

    @Nested
    @DisplayName("getWeekBasedYear")
    class GetWeekBasedYear {

        @Test
        @DisplayName("normal date returns calendar year")
        void normalDate() {
            assertEquals(2026, WeekUtils.getWeekBasedYear(LocalDate.of(2026, 6, 15)));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> WeekUtils.getWeekBasedYear(null));
        }
    }

    @Nested
    @DisplayName("isWeekend / isWeekday")
    class WeekendWeekday {

        @Test
        @DisplayName("Saturday is weekend")
        void saturday() {
            assertTrue(WeekUtils.isWeekend(LocalDate.of(2026, 10, 31))); // Saturday
        }

        @Test
        @DisplayName("Sunday is weekend")
        void sunday() {
            assertTrue(WeekUtils.isWeekend(LocalDate.of(2026, 11, 1))); // Sunday
        }

        @Test
        @DisplayName("Monday is not weekend")
        void monday() {
            assertFalse(WeekUtils.isWeekend(LocalDate.of(2026, 10, 26))); // Monday
        }

        @Test
        @DisplayName("Monday is weekday")
        void mondayIsWeekday() {
            assertTrue(WeekUtils.isWeekday(LocalDate.of(2026, 10, 26)));
        }

        @Test
        @DisplayName("Saturday is not weekday")
        void saturdayNotWeekday() {
            assertFalse(WeekUtils.isWeekday(LocalDate.of(2026, 10, 31)));
        }

        @Test
        @DisplayName("null throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class, () -> WeekUtils.isWeekend(null));
        }
    }
}
