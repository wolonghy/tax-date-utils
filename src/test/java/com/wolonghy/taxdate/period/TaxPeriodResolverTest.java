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

import com.wolonghy.taxdate.constant.PeriodType;
import com.wolonghy.taxdate.constant.QuickPeriod;
import com.wolonghy.taxdate.exception.TaxDateException;
import com.wolonghy.taxdate.model.DeclarationMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TaxPeriodResolver}.
 */
@DisplayName("TaxPeriodResolver")
class TaxPeriodResolverTest {

    // ==================== Period Type Detection ====================

    @Nested
    @DisplayName("detectPeriodType()")
    class DetectPeriodType {

        @Test
        @DisplayName("detects MONTH type")
        void month() {
            assertEquals(PeriodType.MONTH, TaxPeriodResolver.detectPeriodType("202601"));
        }

        @Test
        @DisplayName("detects QUARTER type")
        void quarter() {
            assertEquals(PeriodType.QUARTER, TaxPeriodResolver.detectPeriodType("2026Q1"));
        }

        @Test
        @DisplayName("detects HALF_YEAR type")
        void halfYear() {
            assertEquals(PeriodType.HALF_YEAR, TaxPeriodResolver.detectPeriodType("2026FH"));
            assertEquals(PeriodType.HALF_YEAR, TaxPeriodResolver.detectPeriodType("2026SH"));
        }

        @Test
        @DisplayName("detects YEAR type")
        void year() {
            assertEquals(PeriodType.YEAR, TaxPeriodResolver.detectPeriodType("2026"));
        }

        @Test
        @DisplayName("detects ONCE type")
        void once() {
            assertEquals(PeriodType.ONCE, TaxPeriodResolver.detectPeriodType("202601N1"));
        }
    }

    // ==================== Declaration Cycle ====================

    @Nested
    @DisplayName("getDeclarationCycle()")
    class GetDeclarationCycle {

        @Test
        @DisplayName("4-digit numeric → YEAR")
        void year() {
            assertEquals(PeriodType.YEAR, TaxPeriodResolver.getDeclarationCycle("2026"));
        }

        @Test
        @DisplayName("contains Q → QUARTER")
        void quarter() {
            assertEquals(PeriodType.QUARTER, TaxPeriodResolver.getDeclarationCycle("2026Q1"));
        }

        @Test
        @DisplayName("contains H → HALF_YEAR")
        void halfYear() {
            assertEquals(PeriodType.HALF_YEAR, TaxPeriodResolver.getDeclarationCycle("2026FH"));
            assertEquals(PeriodType.HALF_YEAR, TaxPeriodResolver.getDeclarationCycle("2026SH"));
        }

        @Test
        @DisplayName("contains N → ONCE")
        void once() {
            assertEquals(PeriodType.ONCE, TaxPeriodResolver.getDeclarationCycle("202601N1"));
        }

        @Test
        @DisplayName("6-digit numeric → MONTH")
        void month() {
            assertEquals(PeriodType.MONTH, TaxPeriodResolver.getDeclarationCycle("202601"));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.getDeclarationCycle(null));
        }

        @Test
        @DisplayName("blank input throws TaxDateException")
        void blankInput() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getDeclarationCycle(""));
        }
    }

    // ==================== Declaration Month ====================

    @Nested
    @DisplayName("getDeclarationMonth()")
    class GetDeclarationMonth {

        @Test
        @DisplayName("monthly period: declaration month is the next month")
        void monthly() {
            // 202601 → declared in Feb 2026
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("202601");
            assertEquals(2026, dm.year());
            assertEquals(2, dm.month());
        }

        @Test
        @DisplayName("December monthly: declared in January of next year")
        void decemberMonthly() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("202612");
            assertEquals(2027, dm.year());
            assertEquals(1, dm.month());
        }

        @Test
        @DisplayName("Q1 quarterly: declared in April")
        void q1Quarterly() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("2026Q1");
            assertEquals(2026, dm.year());
            assertEquals(4, dm.month());
        }

        @Test
        @DisplayName("Q4 quarterly: declared in January of next year")
        void q4Quarterly() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("2026Q4");
            assertEquals(2027, dm.year());
            assertEquals(1, dm.month());
        }

        @Test
        @DisplayName("first half: declared in July")
        void firstHalf() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("2026FH");
            assertEquals(2026, dm.year());
            assertEquals(7, dm.month());
        }

        @Test
        @DisplayName("second half: declared in January of next year")
        void secondHalf() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("2026SH");
            assertEquals(2027, dm.year());
            assertEquals(1, dm.month());
        }

        @Test
        @DisplayName("annual: declared in January of next year")
        void annual() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("2026");
            assertEquals(2027, dm.year());
            assertEquals(1, dm.month());
        }

        @Test
        @DisplayName("once: declared in the same month")
        void once() {
            DeclarationMonth dm = TaxPeriodResolver.getDeclarationMonth("202605N1");
            assertEquals(2026, dm.year());
            assertEquals(5, dm.month());
        }
    }

    // ==================== Target Period Resolution ====================

    @Nested
    @DisplayName("getTargetPeriod()")
    class GetTargetPeriod {

        @Test
        @DisplayName("flag '0' returns the same period")
        void currentPeriod() {
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601", "0"));
        }

        @Test
        @DisplayName("flag '1' returns the previous month for monthly period")
        void previousMonth() {
            assertEquals("202512", TaxPeriodResolver.getTargetPeriod("202601", "1"));
            assertEquals("202605", TaxPeriodResolver.getTargetPeriod("202606", "1"));
        }

        @Test
        @DisplayName("flag '2' returns same period last year for monthly")
        void samePeriodLastYear_monthly() {
            assertEquals("202503", TaxPeriodResolver.getTargetPeriod("202603", "2"));
        }

        @Test
        @DisplayName("flag '3' returns December of last year")
        void lastYearEnd() {
            assertEquals("202512", TaxPeriodResolver.getTargetPeriod("202606", "3"));
        }

        @Test
        @DisplayName("flag '1' for quarterly: previous quarter")
        void previousQuarter() {
            assertEquals("2026Q1", TaxPeriodResolver.getTargetPeriod("2026Q2", "1"));
            assertEquals("2025Q4", TaxPeriodResolver.getTargetPeriod("2026Q1", "1"));
        }

        @Test
        @DisplayName("flag '2' for quarterly: same quarter last year")
        void sameQuarterLastYear() {
            assertEquals("2025Q2", TaxPeriodResolver.getTargetPeriod("2026Q2", "2"));
        }

        @Test
        @DisplayName("y-prefixed flag: specific month of the year")
        void yearMonthFlag() {
            assertEquals("202603", TaxPeriodResolver.getTargetPeriod("202601", "y03"));
            assertEquals("202612", TaxPeriodResolver.getTargetPeriod("202606", "y12"));
        }

        @Test
        @DisplayName("q-prefixed flag: specific quarter")
        void quarterFlag() {
            assertEquals("2026Q3", TaxPeriodResolver.getTargetPeriod("202601", "q3"));
        }

        @Test
        @DisplayName("null or blank flag returns the original period")
        void nullOrBlankFlag() {
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601", null));
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601", ""));
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601", "  "));
        }

        @Test
        @DisplayName("null period throws NullPointerException")
        void nullPeriod() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.getTargetPeriod(null, "0"));
        }

        @Test
        @DisplayName("blank period throws TaxDateException")
        void blankPeriod() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getTargetPeriod("", "0"));
        }
    }

    // ==================== Quarter/Month Navigation ====================

    @Nested
    @DisplayName("Navigation")
    class Navigation {

        @Test
        @DisplayName("getQuarterFromMonth converts month to quarter")
        void getQuarterFromMonth() {
            assertEquals("2026Q1", TaxPeriodResolver.getQuarterFromMonth("202601"));
            assertEquals("2026Q1", TaxPeriodResolver.getQuarterFromMonth("202603"));
            assertEquals("2026Q2", TaxPeriodResolver.getQuarterFromMonth("202604"));
            assertEquals("2026Q4", TaxPeriodResolver.getQuarterFromMonth("202612"));
        }

        @Test
        @DisplayName("getQuarterFromMonth throws for invalid format")
        void getQuarterFromMonth_invalid() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getQuarterFromMonth("2026"));
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getQuarterFromMonth("2026Q1"));
        }

        @Test
        @DisplayName("getMonthsInQuarter returns 3 months")
        void getMonthsInQuarter() {
            assertEquals(List.of("202601", "202602", "202603"),
                    TaxPeriodResolver.getMonthsInQuarter("2026Q1"));
            assertEquals(List.of("202604", "202605", "202606"),
                    TaxPeriodResolver.getMonthsInQuarter("2026Q2"));
            assertEquals(List.of("202610", "202611", "202612"),
                    TaxPeriodResolver.getMonthsInQuarter("2026Q4"));
        }

        @Test
        @DisplayName("getPreviousQuarter returns the prior quarter")
        void getPreviousQuarter() {
            assertEquals("2026Q1", TaxPeriodResolver.getPreviousQuarter("2026Q2"));
            assertEquals("2025Q4", TaxPeriodResolver.getPreviousQuarter("2026Q1"));
        }

        @Test
        @DisplayName("getNextQuarter returns the following quarter")
        void getNextQuarter() {
            assertEquals("2026Q3", TaxPeriodResolver.getNextQuarter("2026Q2"));
            assertEquals("2027Q1", TaxPeriodResolver.getNextQuarter("2026Q4"));
        }

        @Test
        @DisplayName("getPreviousMonth returns the prior month")
        void getPreviousMonth() {
            assertEquals("202512", TaxPeriodResolver.getPreviousMonth("202601"));
            assertEquals("202605", TaxPeriodResolver.getPreviousMonth("202606"));
        }

        @Test
        @DisplayName("getNextMonth returns the following month")
        void getNextMonth() {
            assertEquals("202701", TaxPeriodResolver.getNextMonth("202612"));
            assertEquals("202607", TaxPeriodResolver.getNextMonth("202606"));
        }

        @Test
        @DisplayName("navigation methods throw for wrong period type")
        void wrongType() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getPreviousQuarter("202601"));
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getNextMonth("2026Q1"));
        }
    }

    // ==================== getPeriodTypeByValidity ====================

    @Nested
    @DisplayName("getPeriodTypeByValidity()")
    class PeriodTypeByValidity {

        @Test
        @DisplayName("1-month range → MONTH")
        void monthly() {
            assertEquals(PeriodType.MONTH,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-01", "2026-01-31"));
        }

        @Test
        @DisplayName("3-month range → QUARTER")
        void quarterly() {
            assertEquals(PeriodType.QUARTER,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-01", "2026-03-31"));
        }

        @Test
        @DisplayName("6-month range → HALF_YEAR")
        void halfYearly() {
            assertEquals(PeriodType.HALF_YEAR,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-01", "2026-06-30"));
        }

        @Test
        @DisplayName("12-month range → YEAR")
        void annually() {
            assertEquals(PeriodType.YEAR,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-01", "2026-12-31"));
        }

        @Test
        @DisplayName("1-day range → ONCE")
        void once() {
            assertEquals(PeriodType.ONCE,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-15", "2026-01-15"));
        }

        @Test
        @DisplayName("unrecognized range throws TaxDateException")
        void unrecognized() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getPeriodTypeByValidity("2026-01-01", "2026-02-15"));
        }
    }

    // ==================== getCurrentPeriod ====================

    @Nested
    @DisplayName("getCurrentPeriod()")
    class GetCurrentPeriod {

        @Test
        @DisplayName("returns current month in yyyyMM format")
        void returnsCurrentMonth() {
            String result = TaxPeriodResolver.getCurrentPeriod();
            assertNotNull(result);
            assertEquals(6, result.length());
            assertTrue(result.chars().allMatch(Character::isDigit));
        }
    }

    // ==================== resolveQuickPeriod ====================

    @Nested
    @DisplayName("resolveQuickPeriod()")
    class ResolveQuickPeriod {

        @Test
        @DisplayName("CURRENT_MONTH returns current month")
        void currentMonth() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.CURRENT_MONTH);
            assertNotNull(result);
            assertEquals(6, result.length());
        }

        @Test
        @DisplayName("PREVIOUS_MONTH returns previous month")
        void previousMonth() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.PREVIOUS_MONTH);
            assertNotNull(result);
            assertEquals(6, result.length());
        }

        @Test
        @DisplayName("CURRENT_QUARTER returns current quarter")
        void currentQuarter() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.CURRENT_QUARTER);
            assertTrue(result.contains("Q"));
        }

        @Test
        @DisplayName("PREVIOUS_YEAR returns previous year")
        void previousYear() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.PREVIOUS_YEAR);
            assertEquals(String.valueOf(LocalDate.now().getYear() - 1), result);
        }

        @Test
        @DisplayName("FIRST_HALF returns current year first half")
        void firstHalf() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.FIRST_HALF);
            assertTrue(result.endsWith("FH"));
        }

        @Test
        @DisplayName("SECOND_HALF returns current year second half")
        void secondHalf() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.SECOND_HALF);
            assertTrue(result.endsWith("SH"));
        }

        @Test
        @DisplayName("CURRENT_MONTH_OCCURRENCE returns current month with N1")
        void currentMonthOccurrence() {
            String result = TaxPeriodResolver.resolveQuickPeriod(QuickPeriod.CURRENT_MONTH_OCCURRENCE);
            assertTrue(result.endsWith("N1"));
        }

        @Test
        @DisplayName("null quickPeriod throws NullPointerException")
        void nullQuickPeriod() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.resolveQuickPeriod(null));
        }
    }

    // ==================== All Flag Codes ====================

    @Nested
    @DisplayName("Flag codes for all period types")
    class AllFlagCodes {

        @Test
        @DisplayName("monthly flag '4': January generates 202600")
        void monthly_flag4_january() {
            assertEquals("202600", TaxPeriodResolver.getTargetPeriod("202601", "4"));
        }

        @Test
        @DisplayName("monthly flag '9': converts to per-occurrence")
        void monthly_flag9() {
            assertEquals("202601N1", TaxPeriodResolver.getTargetPeriod("202601", "9"));
        }

        @Test
        @DisplayName("quarterly flag '4': Q1 generates 2026Q0")
        void quarterly_flag4_q1() {
            assertEquals("2026Q0", TaxPeriodResolver.getTargetPeriod("2026Q1", "4"));
        }

        @Test
        @DisplayName("quarterly flag '6': last month of quarter")
        void quarterly_flag6() {
            assertEquals("202603", TaxPeriodResolver.getTargetPeriod("2026Q1", "6"));
            assertEquals("202606", TaxPeriodResolver.getTargetPeriod("2026Q2", "6"));
            assertEquals("202609", TaxPeriodResolver.getTargetPeriod("2026Q3", "6"));
            assertEquals("202612", TaxPeriodResolver.getTargetPeriod("2026Q4", "6"));
        }

        @Test
        @DisplayName("half-year flag '1': previous half-year")
        void halfYear_flag1() {
            assertEquals("2025SH", TaxPeriodResolver.getTargetPeriod("2026FH", "1"));
            assertEquals("2026FH", TaxPeriodResolver.getTargetPeriod("2026SH", "1"));
        }

        @Test
        @DisplayName("half-year flag '2': same half last year")
        void halfYear_flag2() {
            assertEquals("2025FH", TaxPeriodResolver.getTargetPeriod("2026FH", "2"));
            assertEquals("2025SH", TaxPeriodResolver.getTargetPeriod("2026SH", "2"));
        }

        @Test
        @DisplayName("half-year flag '3': December of last year")
        void halfYear_flag3() {
            assertEquals("202512", TaxPeriodResolver.getTargetPeriod("2026FH", "3"));
        }

        @Test
        @DisplayName("once flag '5': strips occurrence suffix")
        void once_flag5() {
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601N1", "5"));
        }

        @Test
        @DisplayName("once flag '10': converts to quarter")
        void once_flag10() {
            assertEquals("2026Q1", TaxPeriodResolver.getTargetPeriod("202601N1", "10"));
            assertEquals("2026Q2", TaxPeriodResolver.getTargetPeriod("202604N1", "10"));
        }

        @Test
        @DisplayName("year flag '1': previous year")
        void year_flag1() {
            assertEquals("2025", TaxPeriodResolver.getTargetPeriod("2026", "1"));
        }

        @Test
        @DisplayName("year flag '3': December of last year")
        void year_flag3() {
            assertEquals("202512", TaxPeriodResolver.getTargetPeriod("2026", "3"));
        }

        @Test
        @DisplayName("year flag '4': throws exception")
        void year_flag4() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getTargetPeriod("2026", "4"));
        }

        @Test
        @DisplayName("year flag '7': December of this year")
        void year_flag7() {
            assertEquals("202612", TaxPeriodResolver.getTargetPeriod("2026", "7"));
        }

        @Test
        @DisplayName("year flag '8': Q4 of this year")
        void year_flag8() {
            assertEquals("2026Q4", TaxPeriodResolver.getTargetPeriod("2026", "8"));
        }

        @Test
        @DisplayName("correction flag '0': strips correction suffix")
        void correction_flag0() {
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601G1", "0"));
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601G2", "0"));
        }

        @Test
        @DisplayName("correction flag '11': decrements correction sequence")
        void correction_flag11() {
            assertEquals("202601G1", TaxPeriodResolver.getTargetPeriod("202601G2", "11"));
            assertEquals("202601", TaxPeriodResolver.getTargetPeriod("202601G1", "11"));
        }
    }

    // ==================== getFullPeriodList ====================

    @Nested
    @DisplayName("getFullPeriodList()")
    class GetFullPeriodList {

        @Test
        @DisplayName("returns all periods for a single month")
        void singleMonth() {
            List<String> periods = TaxPeriodResolver.getFullPeriodList("2026-03-01", "2026-03-31");
            assertTrue(periods.contains("202603"));
            assertTrue(periods.contains("2026Q1"));
            assertTrue(periods.contains("2026"));
        }

        @Test
        @DisplayName("returns all periods for a full quarter")
        void fullQuarter() {
            List<String> periods = TaxPeriodResolver.getFullPeriodList("2026-01-01", "2026-03-31");
            assertTrue(periods.contains("202601"));
            assertTrue(periods.contains("202602"));
            assertTrue(periods.contains("202603"));
            assertTrue(periods.contains("2026Q1"));
        }

        @Test
        @DisplayName("returns all periods for a full first half")
        void fullFirstHalf() {
            List<String> periods = TaxPeriodResolver.getFullPeriodList("2026-01-01", "2026-06-30");
            assertTrue(periods.contains("2026FH"));
            assertTrue(periods.contains("2026Q1"));
            assertTrue(periods.contains("2026Q2"));
        }

        @Test
        @DisplayName("returns all periods for a full year")
        void fullYear() {
            List<String> periods = TaxPeriodResolver.getFullPeriodList("2026-01-01", "2026-12-31");
            assertTrue(periods.contains("2026FH"));
            assertTrue(periods.contains("2026SH"));
            assertTrue(periods.contains("2026"));
            assertEquals(12 + 4 + 2 + 1, periods.size());
        }

        @Test
        @DisplayName("throws for start after end")
        void startAfterEnd() {
            assertThrows(IllegalArgumentException.class,
                    () -> TaxPeriodResolver.getFullPeriodList("2026-12-31", "2026-01-01"));
        }

        @Test
        @DisplayName("works with LocalDate overload")
        void localDateOverload() {
            List<String> periods = TaxPeriodResolver.getFullPeriodList(
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
            assertTrue(periods.contains("2026Q1"));
        }
    }

    // ==================== assembleCompletePeriods ====================

    @Nested
    @DisplayName("assembleCompletePeriods()")
    class AssembleCompletePeriods {

        @Test
        @DisplayName("detects complete quarter from 3 months")
        void completeQuarter() {
            List<String> result = TaxPeriodResolver.assembleCompletePeriods(
                    List.of("202601", "202602", "202603"));
            assertTrue(result.contains("2026Q1"));
        }

        @Test
        @DisplayName("detects complete half-year from 6 months")
        void completeHalfYear() {
            List<String> result = TaxPeriodResolver.assembleCompletePeriods(
                    List.of("202601", "202602", "202603", "202604", "202605", "202606"));
            assertTrue(result.contains("2026FH"));
        }

        @Test
        @DisplayName("detects complete year from 12 months")
        void completeYear() {
            List<String> result = TaxPeriodResolver.assembleCompletePeriods(
                    List.of("202601", "202602", "202603", "202604", "202605", "202606",
                            "202607", "202608", "202609", "202610", "202611", "202612"));
            assertTrue(result.contains("2026"));
            assertTrue(result.contains("2026FH"));
            assertTrue(result.contains("2026SH"));
        }

        @Test
        @DisplayName("returns empty list for empty input")
        void emptyInput() {
            List<String> result = TaxPeriodResolver.assembleCompletePeriods(List.of());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("does not detect incomplete quarter")
        void incompleteQuarter() {
            List<String> result = TaxPeriodResolver.assembleCompletePeriods(
                    List.of("202601", "202602"));
            assertFalse(result.contains("2026Q1"));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.assembleCompletePeriods(null));
        }
    }

    // ==================== getLastQuarter ====================

    @Nested
    @DisplayName("getLastQuarter()")
    class GetLastQuarter {

        @Test
        @DisplayName("Q2 month returns Q1")
        void q2ReturnsQ1() {
            assertEquals("2026Q1", TaxPeriodResolver.getLastQuarter("202604"));
        }

        @Test
        @DisplayName("Q1 month returns previous year Q4")
        void q1ReturnsPreviousQ4() {
            assertEquals("2025Q4", TaxPeriodResolver.getLastQuarter("202601"));
        }

        @Test
        @DisplayName("throws for invalid format")
        void invalidFormat() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getLastQuarter("2026")); // too short
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getLastQuarter("20260101")); // too long
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.getLastQuarter(null));
        }
    }

    // ==================== getMonthsInHalfYear ====================

    @Nested
    @DisplayName("getMonthsInHalfYear()")
    class GetMonthsInHalfYear {

        @Test
        @DisplayName("FH returns Jan-Jun")
        void firstHalf() {
            List<String> months = TaxPeriodResolver.getMonthsInHalfYear("2026FH");
            assertEquals(6, months.size());
            assertEquals("202601", months.get(0));
            assertEquals("202606", months.get(5));
        }

        @Test
        @DisplayName("SH returns Jul-Dec")
        void secondHalf() {
            List<String> months = TaxPeriodResolver.getMonthsInHalfYear("2026SH");
            assertEquals(6, months.size());
            assertEquals("202607", months.get(0));
            assertEquals("202612", months.get(5));
        }

        @Test
        @DisplayName("throws for invalid format")
        void invalidFormat() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getMonthsInHalfYear("2026Q1"));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.getMonthsInHalfYear(null));
        }
    }

    // ==================== getMonthsInYear ====================

    @Nested
    @DisplayName("getMonthsInYear()")
    class GetMonthsInYear {

        @Test
        @DisplayName("returns all 12 months")
        void allMonths() {
            List<String> months = TaxPeriodResolver.getMonthsInYear("2026");
            assertEquals(12, months.size());
            assertEquals("202601", months.get(0));
            assertEquals("202612", months.get(11));
        }

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class,
                    () -> TaxPeriodResolver.getMonthsInYear(null));
        }
    }

    // ==================== getQuartersOfYearAsList ====================

    @Nested
    @DisplayName("getQuartersOfYearAsList()")
    class GetQuartersOfYearAsList {

        @Test
        @DisplayName("returns all 4 quarters")
        void allQuarters() {
            List<String> quarters = TaxPeriodResolver.getQuartersOfYearAsList(2026);
            assertEquals(List.of("2026Q1", "2026Q2", "2026Q3", "2026Q4"), quarters);
        }
    }

    // ==================== getHalfYearsOfYear ====================

    @Nested
    @DisplayName("getHalfYearsOfYear()")
    class GetHalfYearsOfYear {

        @Test
        @DisplayName("returns FH and SH")
        void bothHalves() {
            List<String> halves = TaxPeriodResolver.getHalfYearsOfYear(2026);
            assertEquals(List.of("2026FH", "2026SH"), halves);
        }
    }

    // ==================== getPeriodTypeByValidity with tolerance ====================

    @Nested
    @DisplayName("getPeriodTypeByValidity() with tolerance")
    class PeriodTypeByValidityTolerance {

        @Test
        @DisplayName("1-month range with 2-day tolerance → MONTH")
        void monthlyWithTolerance() {
            assertEquals(PeriodType.MONTH,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-02", "2026-01-30", 2));
        }

        @Test
        @DisplayName("3-month range with 2-day tolerance → QUARTER")
        void quarterlyWithTolerance() {
            assertEquals(PeriodType.QUARTER,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-02", "2026-03-30", 2));
        }

        @Test
        @DisplayName("1-day range with tolerance → ONCE")
        void onceWithTolerance() {
            assertEquals(PeriodType.ONCE,
                    TaxPeriodResolver.getPeriodTypeByValidity("2026-01-15", "2026-01-16", 2));
        }

        @Test
        @DisplayName("unrecognized range with tolerance throws TaxDateException")
        void unrecognizedWithTolerance() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getPeriodTypeByValidity("2026-01-01", "2026-02-20", 2));
        }

        @Test
        @DisplayName("works with LocalDate overload")
        void localDateOverload() {
            assertEquals(PeriodType.MONTH,
                    TaxPeriodResolver.getPeriodTypeByValidity(
                            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 2));
        }
    }

    // ==================== y/q flag validation ====================

    @Nested
    @DisplayName("y/q flag validation")
    class YearQuarterFlagValidation {

        @Test
        @DisplayName("y flag with out-of-range month throws")
        void yFlagOutOfRange() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getTargetPeriod("202601", "y13"));
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getTargetPeriod("202601", "y00"));
        }

        @Test
        @DisplayName("q flag with out-of-range quarter throws")
        void qFlagOutOfRange() {
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getTargetPeriod("202601", "q5"));
            assertThrows(TaxDateException.class,
                    () -> TaxPeriodResolver.getTargetPeriod("202601", "q0"));
        }
    }
}
