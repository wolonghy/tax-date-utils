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

package com.wolonghy.taxdate.period;

import com.wolonghy.taxdate.exception.TaxDateException;
import com.wolonghy.taxdate.model.TaxPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TaxPeriodCalculator}.
 */
@DisplayName("TaxPeriodCalculator")
class TaxPeriodCalculatorTest {

    // ==================== Offset Methods ====================

    @Nested
    @DisplayName("Offset methods")
    class Offsets {

        @Test
        @DisplayName("offsetMonths adds months to a monthly period")
        void offsetMonths_positive() {
            TaxPeriod base = TaxPeriod.ofMonth(2026, 1);
            assertEquals("202604", TaxPeriodCalculator.offsetMonths(base, 3).value());
        }

        @Test
        @DisplayName("offsetMonths with negative offset goes backwards")
        void offsetMonths_negative() {
            TaxPeriod base = TaxPeriod.ofMonth(2026, 3);
            assertEquals("202512", TaxPeriodCalculator.offsetMonths(base, -3).value());
        }

        @Test
        @DisplayName("offsetMonths with zero returns equivalent period")
        void offsetMonths_zero() {
            TaxPeriod base = TaxPeriod.ofMonth(2026, 6);
            assertEquals("202606", TaxPeriodCalculator.offsetMonths(base, 0).value());
        }

        @Test
        @DisplayName("offsetMonths throws for non-monthly period")
        void offsetMonths_wrongType() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodCalculator.offsetMonths(TaxPeriod.ofQuarter(2026, 1), 3));
        }

        @Test
        @DisplayName("offsetQuarters adds quarters to a quarterly period")
        void offsetQuarters_positive() {
            TaxPeriod base = TaxPeriod.ofQuarter(2026, 1);
            assertEquals("2026Q3", TaxPeriodCalculator.offsetQuarters(base, 2).value());
        }

        @Test
        @DisplayName("offsetQuarters wraps across year boundary")
        void offsetQuarters_crossYear() {
            TaxPeriod base = TaxPeriod.ofQuarter(2026, 4);
            assertEquals("2027Q1", TaxPeriodCalculator.offsetQuarters(base, 1).value());
        }

        @Test
        @DisplayName("offsetQuarters with negative offset goes backwards")
        void offsetQuarters_negative() {
            TaxPeriod base = TaxPeriod.ofQuarter(2026, 1);
            assertEquals("2025Q4", TaxPeriodCalculator.offsetQuarters(base, -1).value());
        }

        @Test
        @DisplayName("offsetQuarters throws for non-quarterly period")
        void offsetQuarters_wrongType() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodCalculator.offsetQuarters(TaxPeriod.ofMonth(2026, 1), 2));
        }

        @Test
        @DisplayName("offsetYears works for all period types")
        void offsetYears() {
            assertEquals("202801", TaxPeriodCalculator.offsetYears(TaxPeriod.ofMonth(2026, 1), 2).value());
            assertEquals("2028Q1", TaxPeriodCalculator.offsetYears(TaxPeriod.ofQuarter(2026, 1), 2).value());
            assertEquals("2028FH", TaxPeriodCalculator.offsetYears(TaxPeriod.ofHalfYear(2026, true), 2).value());
            assertEquals("2028", TaxPeriodCalculator.offsetYears(TaxPeriod.ofYear(2026), 2).value());
            assertEquals("202805N1", TaxPeriodCalculator.offsetYears(TaxPeriod.ofOnce(2026, 5), 2).value());
        }

        @Test
        @DisplayName("offsetYears with negative offset goes backwards")
        void offsetYears_negative() {
            assertEquals("2024", TaxPeriodCalculator.offsetYears(TaxPeriod.ofYear(2026), -2).value());
        }
    }

    // ==================== Enumeration Methods ====================

    @Nested
    @DisplayName("Enumeration methods")
    class Enumeration {

        @Test
        @DisplayName("enumerateMonths lists all months in range")
        void enumerateMonths() {
            TaxPeriod from = TaxPeriod.ofMonth(2026, 1);
            TaxPeriod to = TaxPeriod.ofMonth(2026, 4);

            List<TaxPeriod> months = TaxPeriodCalculator.enumerateMonths(from, to);
            assertEquals(4, months.size());
            assertEquals("202601", months.get(0).value());
            assertEquals("202602", months.get(1).value());
            assertEquals("202603", months.get(2).value());
            assertEquals("202604", months.get(3).value());
        }

        @Test
        @DisplayName("enumerateMonths with same from and to returns single element")
        void enumerateMonths_singleElement() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 6);
            List<TaxPeriod> months = TaxPeriodCalculator.enumerateMonths(period, period);
            assertEquals(1, months.size());
            assertEquals("202606", months.get(0).value());
        }

        @Test
        @DisplayName("enumerateQuarters lists all quarters in range")
        void enumerateQuarters() {
            TaxPeriod from = TaxPeriod.ofQuarter(2026, 1);
            TaxPeriod to = TaxPeriod.ofQuarter(2026, 4);

            List<TaxPeriod> quarters = TaxPeriodCalculator.enumerateQuarters(from, to);
            assertEquals(4, quarters.size());
            assertEquals("2026Q1", quarters.get(0).value());
            assertEquals("2026Q2", quarters.get(1).value());
            assertEquals("2026Q3", quarters.get(2).value());
            assertEquals("2026Q4", quarters.get(3).value());
        }

        @Test
        @DisplayName("enumerateQuarters crosses year boundary")
        void enumerateQuarters_crossYear() {
            TaxPeriod from = TaxPeriod.ofQuarter(2026, 3);
            TaxPeriod to = TaxPeriod.ofQuarter(2027, 2);

            List<TaxPeriod> quarters = TaxPeriodCalculator.enumerateQuarters(from, to);
            assertEquals(4, quarters.size());
            assertEquals("2026Q3", quarters.get(0).value());
            assertEquals("2026Q4", quarters.get(1).value());
            assertEquals("2027Q1", quarters.get(2).value());
            assertEquals("2027Q2", quarters.get(3).value());
        }

        @Test
        @DisplayName("null arguments throw NullPointerException")
        void nullArguments() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.enumerateMonths(null, period));
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.enumerateMonths(period, null));
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.enumerateQuarters(null, period));
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.enumerateQuarters(period, null));
        }
    }

    // ==================== Between Methods ====================

    @Nested
    @DisplayName("Between methods")
    class Between {

        @Test
        @DisplayName("monthsBetween calculates correctly")
        void monthsBetween() {
            TaxPeriod from = TaxPeriod.ofMonth(2026, 1);
            TaxPeriod to = TaxPeriod.ofMonth(2026, 7);
            assertEquals(6, TaxPeriodCalculator.monthsBetween(from, to));
        }

        @Test
        @DisplayName("monthsBetween returns negative for reversed range")
        void monthsBetween_reversed() {
            TaxPeriod from = TaxPeriod.ofMonth(2026, 7);
            TaxPeriod to = TaxPeriod.ofMonth(2026, 1);
            assertEquals(-6, TaxPeriodCalculator.monthsBetween(from, to));
        }

        @Test
        @DisplayName("quartersBetween calculates correctly")
        void quartersBetween() {
            TaxPeriod from = TaxPeriod.ofQuarter(2026, 1);
            TaxPeriod to = TaxPeriod.ofQuarter(2026, 4);
            assertEquals(3, TaxPeriodCalculator.quartersBetween(from, to));
        }

        @Test
        @DisplayName("quartersBetween crosses year boundary")
        void quartersBetween_crossYear() {
            TaxPeriod from = TaxPeriod.ofQuarter(2026, 3);
            TaxPeriod to = TaxPeriod.ofQuarter(2027, 2);
            assertEquals(3, TaxPeriodCalculator.quartersBetween(from, to));
        }

        @Test
        @DisplayName("offsetMonths with large positive offset")
        void offsetMonths_largePositive() {
            TaxPeriod base = TaxPeriod.ofMonth(2026, 1);
            assertEquals("203601", TaxPeriodCalculator.offsetMonths(base, 120).value());
        }

        @Test
        @DisplayName("offsetMonths with large negative offset")
        void offsetMonths_largeNegative() {
            TaxPeriod base = TaxPeriod.ofMonth(2026, 1);
            assertEquals("201601", TaxPeriodCalculator.offsetMonths(base, -120).value());
        }

        @Test
        @DisplayName("offsetQuarters with large positive offset")
        void offsetQuarters_largePositive() {
            TaxPeriod base = TaxPeriod.ofQuarter(2026, 1);
            assertEquals("2036Q1", TaxPeriodCalculator.offsetQuarters(base, 40).value());
        }

        @Test
        @DisplayName("offsetQuarters with large negative offset")
        void offsetQuarters_largeNegative() {
            TaxPeriod base = TaxPeriod.ofQuarter(2026, 1);
            assertEquals("2016Q1", TaxPeriodCalculator.offsetQuarters(base, -40).value());
        }

        @Test
        @DisplayName("offsetYears with zero returns equivalent period")
        void offsetYears_zero() {
            TaxPeriod base = TaxPeriod.ofMonth(2026, 6);
            assertEquals("202606", TaxPeriodCalculator.offsetYears(base, 0).value());
        }

        @Test
        @DisplayName("offsetMonths null period throws NullPointerException")
        void offsetMonths_null() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.offsetMonths(null, 3));
        }

        @Test
        @DisplayName("offsetQuarters null period throws NullPointerException")
        void offsetQuarters_null() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.offsetQuarters(null, 2));
        }

        @Test
        @DisplayName("offsetYears null period throws NullPointerException")
        void offsetYears_null() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodCalculator.offsetYears(null, 2));
        }

        @Test
        @DisplayName("enumerateQuarters with month-type periods uses default quarters")
        void enumerateQuarters_fromMonthType() {
            // When using month-type periods, quarter defaults to 1 for from and 4 for to
            TaxPeriod from = TaxPeriod.ofMonth(2026, 1);
            TaxPeriod to = TaxPeriod.ofMonth(2026, 12);
            List<TaxPeriod> quarters = TaxPeriodCalculator.enumerateQuarters(from, to);
            assertEquals(4, quarters.size());
            assertEquals("2026Q1", quarters.get(0).value());
            assertEquals("2026Q4", quarters.get(3).value());
        }

        @Test
        @DisplayName("monthsBetween with same period returns zero")
        void monthsBetween_samePeriod() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 6);
            assertEquals(0, TaxPeriodCalculator.monthsBetween(period, period));
        }

        @Test
        @DisplayName("quartersBetween with same quarter returns zero")
        void quartersBetween_sameQuarter() {
            TaxPeriod period = TaxPeriod.ofQuarter(2026, 2);
            assertEquals(0, TaxPeriodCalculator.quartersBetween(period, period));
        }
    }
}
