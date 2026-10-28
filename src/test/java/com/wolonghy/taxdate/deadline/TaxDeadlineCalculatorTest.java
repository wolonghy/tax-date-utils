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

package com.wolonghy.taxdate.deadline;

import com.wolonghy.taxdate.model.TaxPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TaxDeadlineCalculator}.
 */
@DisplayName("TaxDeadlineCalculator")
class TaxDeadlineCalculatorTest {

    // ==================== calculateDeadline ====================

    @Nested
    @DisplayName("calculateDeadline(TaxPeriod)")
    class DefaultDeadline {

        @Test
        @DisplayName("monthly period: deadline is 15th of the declaration month")
        void monthlyPeriod() {
            // 202601 → declaration starts Feb 2026 → deadline Feb 15
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Feb 15, 2026 is a Sunday → adjusted to Monday Feb 16
            assertEquals(LocalDate.of(2026, 2, 16), deadline);
        }

        @Test
        @DisplayName("quarterly period: deadline is 15th of the declaration month")
        void quarterlyPeriod() {
            // 2026Q1 → declaration starts Apr 2026 → deadline Apr 15
            TaxPeriod period = TaxPeriod.ofQuarter(2026, 1);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Apr 15, 2026 is a Wednesday → no adjustment
            assertEquals(LocalDate.of(2026, 4, 15), deadline);
        }

        @Test
        @DisplayName("annual period: declaration starts Jan of next year")
        void annualPeriod() {
            TaxPeriod period = TaxPeriod.ofYear(2026);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Jan 15, 2027 is a Friday → no adjustment
            assertEquals(LocalDate.of(2027, 1, 15), deadline);
        }

        @Test
        @DisplayName("null period throws NullPointerException")
        void nullPeriod() {
            assertThrows(NullPointerException.class,
                    () -> TaxDeadlineCalculator.calculateDeadline(null));
        }
    }

    // ==================== calculateDeadline with custom day ====================

    @Nested
    @DisplayName("calculateDeadline(TaxPeriod, int)")
    class CustomDeadline {

        @Test
        @DisplayName("custom deadline day is used")
        void customDay() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 3);
            // Declaration month: April 2026, deadline day: 20
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period, 20);
            // Apr 20, 2026 is a Monday → no adjustment
            assertEquals(LocalDate.of(2026, 4, 20), deadline);
        }

        @Test
        @DisplayName("deadline day exceeds month length → clamped to last day")
        void dayExceedsMonthLength() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            // Declaration month: Feb 2026 (28 days), deadline day: 30 → clamped to 28
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period, 30);
            // Feb 28, 2026 is a Saturday → adjusted to Mar 2 (Monday)
            assertEquals(LocalDate.of(2026, 3, 2), deadline);
        }

        @Test
        @DisplayName("invalid deadline day throws IllegalArgumentException")
        void invalidDay() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDeadlineCalculator.calculateDeadline(period, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDeadlineCalculator.calculateDeadline(period, 32));
        }
    }

    // ==================== adjustForWeekend ====================

    @Nested
    @DisplayName("adjustForWeekend()")
    class WeekendAdjustment {

        @Test
        @DisplayName("Saturday → next Monday")
        void saturday() {
            // 2026-01-17 is a Saturday
            LocalDate saturday = LocalDate.of(2026, 1, 17);
            assertEquals(DayOfWeek.SATURDAY, saturday.getDayOfWeek());
            assertEquals(LocalDate.of(2026, 1, 19),
                    TaxDeadlineCalculator.adjustForWeekend(saturday));
        }

        @Test
        @DisplayName("Sunday → next Monday")
        void sunday() {
            // 2026-02-15 is a Sunday
            LocalDate sunday = LocalDate.of(2026, 2, 15);
            assertEquals(DayOfWeek.SUNDAY, sunday.getDayOfWeek());
            assertEquals(LocalDate.of(2026, 2, 16),
                    TaxDeadlineCalculator.adjustForWeekend(sunday));
        }

        @Test
        @DisplayName("weekday → unchanged")
        void weekday() {
            // 2026-02-16 is a Monday
            LocalDate monday = LocalDate.of(2026, 2, 16);
            assertEquals(DayOfWeek.MONDAY, monday.getDayOfWeek());
            assertEquals(monday, TaxDeadlineCalculator.adjustForWeekend(monday));
        }

        @Test
        @DisplayName("null date throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class,
                    () -> TaxDeadlineCalculator.adjustForWeekend(null));
        }
    }

    // ==================== adjustForWeekendAndHoliday ====================

    @Nested
    @DisplayName("adjustForWeekendAndHoliday()")
    class HolidayAdjustment {

        @Test
        @DisplayName("date on holiday → advances to next business day")
        void holidayAdjustment() {
            // 2026-02-16 is a Monday; assume it's a holiday
            LocalDate monday = LocalDate.of(2026, 2, 16);
            Set<LocalDate> holidays = Set.of(monday);
            assertEquals(LocalDate.of(2026, 2, 17),
                    TaxDeadlineCalculator.adjustForWeekendAndHoliday(monday, holidays));
        }

        @Test
        @DisplayName("date on holiday that's followed by weekend → skips to Monday")
        void holidayBeforeWeekend() {
            // 2026-02-14 is a Saturday → adjusted to Monday Feb 16
            // If Feb 16 is also a holiday → advances to Feb 17
            LocalDate saturday = LocalDate.of(2026, 2, 14);
            Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 2, 16));
            assertEquals(LocalDate.of(2026, 2, 17),
                    TaxDeadlineCalculator.adjustForWeekendAndHoliday(saturday, holidays));
        }

        @Test
        @DisplayName("null holidays set is treated as empty")
        void nullHolidays() {
            LocalDate friday = LocalDate.of(2026, 2, 13);
            assertEquals(friday,
                    TaxDeadlineCalculator.adjustForWeekendAndHoliday(friday, null));
        }

        @Test
        @DisplayName("non-holiday weekday is unchanged")
        void nonHoliday() {
            LocalDate friday = LocalDate.of(2026, 2, 13);
            Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 2, 14));
            assertEquals(friday,
                    TaxDeadlineCalculator.adjustForWeekendAndHoliday(friday, holidays));
        }
    }

    // ==================== getNaturalDeadlineDay ====================

    @Nested
    @DisplayName("getNaturalDeadlineDay()")
    class NaturalDeadline {

        @Test
        @DisplayName("monthly period → 15")
        void monthly() {
            assertEquals(15, TaxDeadlineCalculator.getNaturalDeadlineDay(
                    TaxPeriod.ofMonth(2026, 1)));
        }

        @Test
        @DisplayName("quarterly period → 15")
        void quarterly() {
            assertEquals(15, TaxDeadlineCalculator.getNaturalDeadlineDay(
                    TaxPeriod.ofQuarter(2026, 1)));
        }

        @Test
        @DisplayName("half-year period → 15")
        void halfYearly() {
            assertEquals(15, TaxDeadlineCalculator.getNaturalDeadlineDay(
                    TaxPeriod.ofHalfYear(2026, true)));
        }

        @Test
        @DisplayName("past annual period → 31")
        void pastAnnual() {
            assertEquals(31, TaxDeadlineCalculator.getNaturalDeadlineDay(
                    TaxPeriod.ofYear(2020)));
        }

        @Test
        @DisplayName("once period → 15")
        void once() {
            assertEquals(15, TaxDeadlineCalculator.getNaturalDeadlineDay(
                    TaxPeriod.ofOnce(2026, 5)));
        }

        @Test
        @DisplayName("current year annual period → 15")
        void currentYearAnnual() {
            int currentYear = LocalDate.now().getYear();
            assertEquals(15, TaxDeadlineCalculator.getNaturalDeadlineDay(
                    TaxPeriod.ofYear(currentYear)));
        }
    }

    // ==================== adjustForChineseHoliday ====================

    @Nested
    @DisplayName("adjustForChineseHoliday()")
    class ChineseHolidayAdjustment {

        @Test
        @DisplayName("holiday → advances to next business day")
        void holiday() {
            LocalDate date = LocalDate.of(2026, 2, 16); // Monday
            Set<LocalDate> holidays = Set.of(date);
            assertEquals(LocalDate.of(2026, 2, 17),
                    TaxDeadlineCalculator.adjustForChineseHoliday(date, holidays, null));
        }

        @Test
        @DisplayName("weekend without makeup workday → advances to Monday")
        void weekendWithoutMakeup() {
            LocalDate saturday = LocalDate.of(2026, 2, 14);
            assertEquals(LocalDate.of(2026, 2, 16),
                    TaxDeadlineCalculator.adjustForChineseHoliday(saturday, null, null));
        }

        @Test
        @DisplayName("weekend with makeup workday → unchanged")
        void weekendWithMakeup() {
            LocalDate saturday = LocalDate.of(2026, 2, 14);
            Set<LocalDate> makeupWorkdays = Set.of(saturday);
            assertEquals(saturday,
                    TaxDeadlineCalculator.adjustForChineseHoliday(saturday, null, makeupWorkdays));
        }

        @Test
        @DisplayName("consecutive holidays → advances past all")
        void consecutiveHolidays() {
            LocalDate start = LocalDate.of(2026, 2, 16); // Monday
            Set<LocalDate> holidays = Set.of(
                    start,
                    start.plusDays(1), // Tuesday
                    start.plusDays(2)  // Wednesday
            );
            assertEquals(LocalDate.of(2026, 2, 19),
                    TaxDeadlineCalculator.adjustForChineseHoliday(start, holidays, null));
        }

        @Test
        @DisplayName("holiday followed by weekend → skips to Monday")
        void holidayThenWeekend() {
            LocalDate friday = LocalDate.of(2026, 2, 13);
            Set<LocalDate> holidays = Set.of(friday);
            // Friday is holiday → Saturday → Sunday → Monday
            assertEquals(LocalDate.of(2026, 2, 16),
                    TaxDeadlineCalculator.adjustForChineseHoliday(friday, holidays, null));
        }

        @Test
        @DisplayName("null holidays and makeupWorkdays → only weekend adjustment")
        void nullSets() {
            LocalDate saturday = LocalDate.of(2026, 2, 14);
            assertEquals(LocalDate.of(2026, 2, 16),
                    TaxDeadlineCalculator.adjustForChineseHoliday(saturday, null, null));
        }

        @Test
        @DisplayName("null date throws NullPointerException")
        void nullDate() {
            assertThrows(NullPointerException.class,
                    () -> TaxDeadlineCalculator.adjustForChineseHoliday(null, null, null));
        }
    }

    // ==================== calculateDeadlineWithChineseHoliday ====================

    @Nested
    @DisplayName("calculateDeadlineWithChineseHoliday()")
    class ChineseHolidayDeadline {

        @Test
        @DisplayName("deadline on holiday → advances to next business day")
        void deadlineOnHoliday() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            // Declaration month: Feb 2026, deadline: Feb 15 (Sunday → Monday Feb 16)
            // If Feb 16 is a holiday → advances to Feb 17
            Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 2, 16));
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
                    period, holidays, null);
            assertEquals(LocalDate.of(2026, 2, 17), deadline);
        }

        @Test
        @DisplayName("deadline on makeup workday → unchanged")
        void deadlineOnMakeupWorkday() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            // Feb 15 is Sunday → would be adjusted to Feb 16
            // If Feb 14 (Saturday) is a makeup workday, it doesn't affect Feb 16
            Set<LocalDate> makeupWorkdays = Set.of(LocalDate.of(2026, 2, 14));
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
                    period, makeupWorkdays, null);
            // Feb 15 (Sunday) → Feb 16 (Monday), not a makeup workday
            assertEquals(LocalDate.of(2026, 2, 16), deadline);
        }

        @Test
        @DisplayName("custom deadline day with holiday adjustment")
        void customDayWithHoliday() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 3);
            // Declaration month: April 2026, deadline day: 20
            // Apr 20, 2026 is a Monday
            Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 4, 20));
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
                    period, 20, holidays, null);
            assertEquals(LocalDate.of(2026, 4, 21), deadline);
        }

        @Test
        @DisplayName("null period throws NullPointerException")
        void nullPeriod() {
            assertThrows(NullPointerException.class,
                    () -> TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
                            null, null, null));
        }

        @Test
        @DisplayName("invalid deadline day throws IllegalArgumentException")
        void invalidDay() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
                            period, 0, null, null));
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
                            period, 32, null, null));
        }
    }

    // ==================== calculateDeadline for all period types ====================

    @Nested
    @DisplayName("calculateDeadline() for all period types")
    class AllPeriodTypes {

        @Test
        @DisplayName("half-year FH: deadline is July 15")
        void halfYearFH() {
            TaxPeriod period = TaxPeriod.ofHalfYear(2026, true);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Jul 15, 2026 is a Wednesday → no adjustment
            assertEquals(LocalDate.of(2026, 7, 15), deadline);
        }

        @Test
        @DisplayName("half-year SH: deadline is Jan 15 of next year")
        void halfYearSH() {
            TaxPeriod period = TaxPeriod.ofHalfYear(2026, false);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Jan 15, 2027 is a Friday → no adjustment
            assertEquals(LocalDate.of(2027, 1, 15), deadline);
        }

        @Test
        @DisplayName("once period: deadline is 15th of the same month")
        void oncePeriod() {
            TaxPeriod period = TaxPeriod.ofOnce(2026, 3);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Mar 15, 2026 is a Sunday → adjusted to Monday Mar 16
            assertEquals(LocalDate.of(2026, 3, 16), deadline);
        }

        @Test
        @DisplayName("December monthly: deadline is Jan 15 of next year")
        void decemberMonthly() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 12);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Jan 15, 2027 is a Friday → no adjustment
            assertEquals(LocalDate.of(2027, 1, 15), deadline);
        }

        @Test
        @DisplayName("Q4 quarterly: deadline is Jan 15 of next year")
        void q4Quarterly() {
            TaxPeriod period = TaxPeriod.ofQuarter(2026, 4);
            LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);
            // Jan 15, 2027 is a Friday → no adjustment
            assertEquals(LocalDate.of(2027, 1, 15), deadline);
        }
    }
}
