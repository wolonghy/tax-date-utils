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

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TaxDateUtils}.
 */
@DisplayName("TaxDateUtils")
class TaxDateUtilsTest {

    // ==================== Conversion ====================

    @Nested
    @DisplayName("Conversion methods")
    class Conversion {

        @Test
        @DisplayName("toLocalDate(long) converts epoch millis to date")
        void toLocalDate_fromEpochMilli() {
            // 2026-01-15 in epoch millis
            long millis = LocalDate.of(2026, 1, 15).atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            assertEquals(LocalDate.of(2026, 1, 15), TaxDateUtils.toLocalDate(millis));
        }

        @Test
        @DisplayName("toLocalDateTime(LocalDate) converts to midnight")
        void toLocalDateTime_fromLocalDate() {
            LocalDate date = LocalDate.of(2026, 1, 15);
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0, 0), TaxDateUtils.toLocalDateTime(date));
        }

        @Test
        @DisplayName("toLocalDateTime(LocalDate, hour, minute, second) converts with specific time")
        void toLocalDateTime_fromLocalDate_withTime() {
            LocalDate date = LocalDate.of(2026, 1, 15);
            assertEquals(LocalDateTime.of(2026, 1, 15, 14, 30, 45),
                    TaxDateUtils.toLocalDateTime(date, 14, 30, 45));
        }

        @Test
        @DisplayName("toLocalDate(LocalDateTime) extracts date part")
        void toLocalDate_fromLocalDateTime() {
            LocalDateTime dt = LocalDateTime.of(2026, 1, 15, 14, 30, 45);
            assertEquals(LocalDate.of(2026, 1, 15), TaxDateUtils.toLocalDate(dt));
        }

        @Test
        @DisplayName("conversion methods throw on null")
        void conversion_nullArguments() {
            assertThrows(NullPointerException.class, () -> TaxDateUtils.toLocalDateTime((LocalDate) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.toLocalDateTime(null, 0, 0, 0));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.toLocalDate((LocalDateTime) null));
        }
    }

    // ==================== Formatting ====================

    @Nested
    @DisplayName("Formatting methods")
    class Formatting {

        @Test
        @DisplayName("formatDate produces yyyy-MM-dd")
        void formatDate() {
            assertEquals("2026-01-15", TaxDateUtils.formatDate(LocalDate.of(2026, 1, 15)));
        }

        @Test
        @DisplayName("formatMonth produces yyyyMM")
        void formatMonth() {
            assertEquals("202601", TaxDateUtils.formatMonth(LocalDate.of(2026, 1, 15)));
            assertEquals("202612", TaxDateUtils.formatMonth(LocalDate.of(2026, 12, 1)));
        }

        @Test
        @DisplayName("formatMonthDash produces yyyy-MM")
        void formatMonthDash() {
            assertEquals("2026-01", TaxDateUtils.formatMonthDash(LocalDate.of(2026, 1, 15)));
        }

        @Test
        @DisplayName("format with custom pattern works")
        void formatCustomPattern() {
            assertEquals("2026/01/15",
                    TaxDateUtils.format(LocalDate.of(2026, 1, 15), "yyyy/MM/dd"));
        }

        @Test
        @DisplayName("format LocalDateTime with custom pattern")
        void formatLocalDateTime() {
            LocalDateTime ldt = LocalDateTime.of(2026, 1, 15, 14, 30, 45);
            assertEquals("2026.01.15.14:30:45",
                    TaxDateUtils.format(ldt, TaxDateUtils.PATTERN_DATETIME_DOT));
        }

        @Test
        @DisplayName("LocalDate convenience format methods cover all patterns")
        void localDate_allFormats() {
            LocalDate d = LocalDate.of(2026, 1, 15);
            assertEquals("2026-01-15", TaxDateUtils.formatDate(d));
            assertEquals("202601", TaxDateUtils.formatMonth(d));
            assertEquals("2026-01", TaxDateUtils.formatMonthDash(d));
            assertEquals("20260115", TaxDateUtils.formatDateCompact(d));
            assertEquals("2026/01/15", TaxDateUtils.formatDateSlash(d));
            assertEquals("2026.01.15", TaxDateUtils.formatDateDot(d));
            assertEquals("260115", TaxDateUtils.formatDateShort(d));
            assertEquals("2026", TaxDateUtils.formatYear(d));
        }

        @Test
        @DisplayName("LocalDateTime convenience format methods cover all patterns")
        void localDateTime_allFormats() {
            LocalDateTime dt = LocalDateTime.of(2026, 1, 15, 14, 30, 45);
            assertEquals("2026-01-15", TaxDateUtils.formatDate(dt));
            assertEquals("202601", TaxDateUtils.formatMonth(dt));
            assertEquals("2026-01", TaxDateUtils.formatMonthDash(dt));
            assertEquals("20260115", TaxDateUtils.formatDateCompact(dt));
            assertEquals("2026/01/15", TaxDateUtils.formatDateSlash(dt));
            assertEquals("2026.01.15", TaxDateUtils.formatDateDot(dt));
            assertEquals("260115", TaxDateUtils.formatDateShort(dt));
            assertEquals("2026", TaxDateUtils.formatYear(dt));
            assertEquals("2026-01-15 14:30:45", TaxDateUtils.formatDateTime(dt));
            assertEquals("20260115143045", TaxDateUtils.formatDateTimeCompact(dt));
            assertEquals("2026年01月15日 14:30:45", TaxDateUtils.formatDateTimeCn(dt));
            assertEquals("2026年01月15日 14时30分45秒", TaxDateUtils.formatDateTimeCnFull(dt));
            assertEquals("2026.01.15.14:30:45", TaxDateUtils.formatDateTimeDot(dt));
            assertEquals("2026-01-15 14:30", TaxDateUtils.formatDateTimeMinute(dt));
        }

        @Test
        @DisplayName("formatDateTimeMs produces millisecond timestamp")
        void formatDateTimeMs() {
            LocalDateTime dt = LocalDateTime.of(2026, 1, 15, 14, 30, 45);
            String result = TaxDateUtils.formatDateTimeMs(dt);
            assertTrue(result.startsWith("20260115143045"));
            assertEquals(17, result.length());
        }
    }

    // ==================== Component Extraction ====================

    @Nested
    @DisplayName("Component extraction methods")
    class ComponentExtraction {

        @Test
        @DisplayName("getYearStr extracts 4-digit year from LocalDate")
        void getYearStr_localDate() {
            assertEquals("2026", TaxDateUtils.getYearStr(LocalDate.of(2026, 3, 15)));
            assertEquals("0099", TaxDateUtils.getYearStr(LocalDate.of(99, 3, 15)));
        }

        @Test
        @DisplayName("getMonthStr extracts 2-digit month from LocalDate")
        void getMonthStr_localDate() {
            assertEquals("01", TaxDateUtils.getMonthStr(LocalDate.of(2026, 1, 15)));
            assertEquals("02", TaxDateUtils.getMonthStr(LocalDate.of(2026, 2, 28)));
            assertEquals("12", TaxDateUtils.getMonthStr(LocalDate.of(2026, 12, 31)));
        }

        @Test
        @DisplayName("getDayStr extracts 2-digit day from LocalDate")
        void getDayStr_localDate() {
            assertEquals("01", TaxDateUtils.getDayStr(LocalDate.of(2026, 1, 1)));
            assertEquals("15", TaxDateUtils.getDayStr(LocalDate.of(2026, 1, 15)));
            assertEquals("31", TaxDateUtils.getDayStr(LocalDate.of(2026, 12, 31)));
        }

        @Test
        @DisplayName("getYearStr extracts 4-digit year from LocalDateTime")
        void getYearStr_localDateTime() {
            assertEquals("2026", TaxDateUtils.getYearStr(LocalDateTime.of(2026, 3, 15, 14, 30, 45)));
        }

        @Test
        @DisplayName("getMonthStr extracts 2-digit month from LocalDateTime")
        void getMonthStr_localDateTime() {
            assertEquals("03", TaxDateUtils.getMonthStr(LocalDateTime.of(2026, 3, 15, 14, 30, 45)));
        }

        @Test
        @DisplayName("getDayStr extracts 2-digit day from LocalDateTime")
        void getDayStr_localDateTime() {
            assertEquals("15", TaxDateUtils.getDayStr(LocalDateTime.of(2026, 3, 15, 14, 30, 45)));
        }

        @Test
        @DisplayName("getHourStr extracts 2-digit hour from LocalDateTime")
        void getHourStr() {
            assertEquals("00", TaxDateUtils.getHourStr(LocalDateTime.of(2026, 1, 1, 0, 0, 0)));
            assertEquals("09", TaxDateUtils.getHourStr(LocalDateTime.of(2026, 1, 1, 9, 30, 45)));
            assertEquals("14", TaxDateUtils.getHourStr(LocalDateTime.of(2026, 1, 1, 14, 30, 45)));
            assertEquals("23", TaxDateUtils.getHourStr(LocalDateTime.of(2026, 1, 1, 23, 59, 59)));
        }

        @Test
        @DisplayName("getMinuteStr extracts 2-digit minute from LocalDateTime")
        void getMinuteStr() {
            assertEquals("00", TaxDateUtils.getMinuteStr(LocalDateTime.of(2026, 1, 1, 0, 0, 0)));
            assertEquals("05", TaxDateUtils.getMinuteStr(LocalDateTime.of(2026, 1, 1, 14, 5, 45)));
            assertEquals("30", TaxDateUtils.getMinuteStr(LocalDateTime.of(2026, 1, 1, 14, 30, 45)));
            assertEquals("59", TaxDateUtils.getMinuteStr(LocalDateTime.of(2026, 1, 1, 23, 59, 59)));
        }

        @Test
        @DisplayName("getSecondStr extracts 2-digit second from LocalDateTime")
        void getSecondStr() {
            assertEquals("00", TaxDateUtils.getSecondStr(LocalDateTime.of(2026, 1, 1, 0, 0, 0)));
            assertEquals("05", TaxDateUtils.getSecondStr(LocalDateTime.of(2026, 1, 1, 14, 30, 5)));
            assertEquals("45", TaxDateUtils.getSecondStr(LocalDateTime.of(2026, 1, 1, 14, 30, 45)));
            assertEquals("59", TaxDateUtils.getSecondStr(LocalDateTime.of(2026, 1, 1, 23, 59, 59)));
        }

        @Test
        @DisplayName("component extraction methods throw on null")
        void componentExtraction_nullArguments() {
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getYearStr((LocalDate) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getMonthStr((LocalDate) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getDayStr((LocalDate) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getYearStr((LocalDateTime) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getMonthStr((LocalDateTime) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getDayStr((LocalDateTime) null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getHourStr(null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getMinuteStr(null));
            assertThrows(NullPointerException.class, () -> TaxDateUtils.getSecondStr(null));
        }
    }

    // ==================== Parsing ====================

    @Nested
    @DisplayName("Parsing methods")
    class Parsing {

        @Test
        @DisplayName("parseLocalDate with default pattern")
        void parseLocalDate_default() {
            assertEquals(LocalDate.of(2026, 1, 15), TaxDateUtils.parseLocalDate("2026-01-15"));
        }

        @Test
        @DisplayName("parseLocalDate with custom pattern")
        void parseLocalDate_custom() {
            assertEquals(LocalDate.of(2026, 1, 15),
                    TaxDateUtils.parseLocalDate("2026/01/15", "yyyy/MM/dd"));
        }

        @Test
        @DisplayName("parseMonth parses yyyyMM to first day of month")
        void parseMonth() {
            assertEquals(LocalDate.of(2026, 1, 1), TaxDateUtils.parseMonth("202601"));
        }

        @Test
        @DisplayName("parseLocalDateTime with pattern")
        void parseLocalDateTime() {
            LocalDateTime expected = LocalDateTime.of(2026, 1, 15, 14, 30, 0);
            assertEquals(expected,
                    TaxDateUtils.parseLocalDateTime("2026-01-15 14:30:00", "yyyy-MM-dd HH:mm:ss"));
        }

        @Test
        @DisplayName("invalid date string throws IllegalArgumentException")
        void invalidDate() {
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDateUtils.parseLocalDate("not-a-date"));
        }
    }

    // ==================== Calculation ====================

    @Nested
    @DisplayName("Date calculation methods")
    class Calculation {

        @Test
        @DisplayName("offsetYears adds/subtracts years")
        void offsetYears() {
            LocalDate base = LocalDate.of(2026, 6, 15);
            assertEquals(LocalDate.of(2028, 6, 15), TaxDateUtils.offsetYears(base, 2));
            assertEquals(LocalDate.of(2024, 6, 15), TaxDateUtils.offsetYears(base, -2));
        }

        @Test
        @DisplayName("offsetMonths adds/subtracts months")
        void offsetMonths() {
            LocalDate base = LocalDate.of(2026, 1, 15);
            assertEquals(LocalDate.of(2026, 4, 15), TaxDateUtils.offsetMonths(base, 3));
            assertEquals(LocalDate.of(2025, 11, 15), TaxDateUtils.offsetMonths(base, -2));
        }

        @Test
        @DisplayName("offsetDays adds/subtracts days")
        void offsetDays() {
            LocalDate base = LocalDate.of(2026, 1, 15);
            assertEquals(LocalDate.of(2026, 1, 25), TaxDateUtils.offsetDays(base, 10));
            assertEquals(LocalDate.of(2026, 1, 5), TaxDateUtils.offsetDays(base, -10));
        }

        @Test
        @DisplayName("getFirstDayOfMonth returns the 1st")
        void getFirstDayOfMonth() {
            assertEquals(LocalDate.of(2026, 3, 1), TaxDateUtils.getFirstDayOfMonth(2026, 3));
        }

        @Test
        @DisplayName("getLastDayOfMonth handles all months correctly")
        void getLastDayOfMonth() {
            assertEquals(LocalDate.of(2026, 1, 31), TaxDateUtils.getLastDayOfMonth(2026, 1));
            assertEquals(LocalDate.of(2026, 2, 28), TaxDateUtils.getLastDayOfMonth(2026, 2));
            assertEquals(LocalDate.of(2024, 2, 29), TaxDateUtils.getLastDayOfMonth(2024, 2)); // leap year
            assertEquals(LocalDate.of(2026, 12, 31), TaxDateUtils.getLastDayOfMonth(2026, 12));
        }

        @Test
        @DisplayName("getLastDayOfMonthStr returns formatted string")
        void getLastDayOfMonthStr() {
            assertEquals("2026-02-28", TaxDateUtils.getLastDayOfMonthStr(2026, 2));
        }

        @Test
        @DisplayName("getFirstDayOfQuarter returns correct dates")
        void getFirstDayOfQuarter() {
            assertEquals(LocalDate.of(2026, 1, 1), TaxDateUtils.getFirstDayOfQuarter(2026, 1));
            assertEquals(LocalDate.of(2026, 4, 1), TaxDateUtils.getFirstDayOfQuarter(2026, 2));
            assertEquals(LocalDate.of(2026, 7, 1), TaxDateUtils.getFirstDayOfQuarter(2026, 3));
            assertEquals(LocalDate.of(2026, 10, 1), TaxDateUtils.getFirstDayOfQuarter(2026, 4));
        }

        @Test
        @DisplayName("getLastDayOfQuarter returns correct dates")
        void getLastDayOfQuarter() {
            assertEquals(LocalDate.of(2026, 3, 31), TaxDateUtils.getLastDayOfQuarter(2026, 1));
            assertEquals(LocalDate.of(2026, 6, 30), TaxDateUtils.getLastDayOfQuarter(2026, 2));
            assertEquals(LocalDate.of(2026, 9, 30), TaxDateUtils.getLastDayOfQuarter(2026, 3));
            assertEquals(LocalDate.of(2026, 12, 31), TaxDateUtils.getLastDayOfQuarter(2026, 4));
        }

        @Test
        @DisplayName("invalid quarter throws IllegalArgumentException")
        void invalidQuarter() {
            assertThrows(IllegalArgumentException.class, () -> TaxDateUtils.getFirstDayOfQuarter(2026, 0));
            assertThrows(IllegalArgumentException.class, () -> TaxDateUtils.getFirstDayOfQuarter(2026, 5));
            assertThrows(IllegalArgumentException.class, () -> TaxDateUtils.getLastDayOfQuarter(2026, 0));
        }

        @Test
        @DisplayName("half-year boundary methods return correct dates")
        void halfYearBoundaries() {
            assertEquals(LocalDate.of(2026, 1, 1), TaxDateUtils.getFirstDayOfFirstHalf(2026));
            assertEquals(LocalDate.of(2026, 6, 30), TaxDateUtils.getLastDayOfFirstHalf(2026));
            assertEquals(LocalDate.of(2026, 7, 1), TaxDateUtils.getFirstDayOfSecondHalf(2026));
            assertEquals(LocalDate.of(2026, 12, 31), TaxDateUtils.getLastDayOfSecondHalf(2026));
        }
    }

    // ==================== Judgment ====================

    @Nested
    @DisplayName("Judgment methods")
    class Judgment {

        @Test
        @DisplayName("isLeapYear detects leap years correctly")
        void isLeapYear() {
            assertTrue(TaxDateUtils.isLeapYear(2024));
            assertFalse(TaxDateUtils.isLeapYear(2026));
            assertFalse(TaxDateUtils.isLeapYear(1900)); // divisible by 100 but not 400
            assertTrue(TaxDateUtils.isLeapYear(2000));  // divisible by 400
        }

        @Test
        @DisplayName("isBetween checks inclusive range")
        void isBetween() {
            LocalDate start = LocalDate.of(2026, 3, 1);
            LocalDate end = LocalDate.of(2026, 5, 31);

            assertTrue(TaxDateUtils.isBetween(LocalDate.of(2026, 4, 15), start, end));
            assertTrue(TaxDateUtils.isBetween(start, start, end));   // boundary
            assertTrue(TaxDateUtils.isBetween(end, start, end));     // boundary
            assertFalse(TaxDateUtils.isBetween(LocalDate.of(2026, 2, 28), start, end));
            assertFalse(TaxDateUtils.isBetween(LocalDate.of(2026, 6, 1), start, end));
        }

        @Test
        @DisplayName("isOverlap detects overlapping date ranges")
        void isOverlap_localDate() {
            assertTrue(TaxDateUtils.isOverlap(
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30)));
            assertFalse(TaxDateUtils.isOverlap(
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30)));
        }

        @Test
        @DisplayName("isOverlap with string arguments")
        void isOverlap_strings() {
            assertTrue(TaxDateUtils.isOverlap("2026-01-01", "2026-06-30", "2026-03-01", "2026-09-30"));
            assertFalse(TaxDateUtils.isOverlap("2026-01-01", "2026-03-31", "2026-04-01", "2026-06-30"));
        }

        @Test
        @DisplayName("isValidDate validates date strings")
        void isValidDate() {
            assertTrue(TaxDateUtils.isValidDate("2026-01-15"));
            assertTrue(TaxDateUtils.isValidDate("2026-12-31"));
            assertFalse(TaxDateUtils.isValidDate("2026-02-29")); // 2026 is not a leap year
            assertFalse(TaxDateUtils.isValidDate("not-a-date"));
            assertFalse(TaxDateUtils.isValidDate(null));
            assertFalse(TaxDateUtils.isValidDate(""));
            assertFalse(TaxDateUtils.isValidDate("2026-01-15", null));
        }

        @Test
        @DisplayName("isValidDate with custom pattern")
        void isValidDate_customPattern() {
            assertTrue(TaxDateUtils.isValidDate("2026/01/15", "yyyy/MM/dd"));
            assertFalse(TaxDateUtils.isValidDate("2026-01-15", "yyyy/MM/dd"));
        }
    }

    // ==================== Difference ====================

    @Nested
    @DisplayName("Difference methods")
    class Difference {

        @Test
        @DisplayName("daysBetween calculates correctly")
        void daysBetween_localDate() {
            assertEquals(30, TaxDateUtils.daysBetween(
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
        }

        @Test
        @DisplayName("daysBetween with string arguments")
        void daysBetween_strings() {
            assertEquals(365, TaxDateUtils.daysBetween("2026-01-01", "2027-01-01"));
        }

        @Test
        @DisplayName("monthsBetween calculates correctly")
        void monthsBetween() {
            assertEquals(5, TaxDateUtils.monthsBetween(
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)));
        }

        @Test
        @DisplayName("yearsBetween calculates correctly")
        void yearsBetween() {
            assertEquals(3, TaxDateUtils.yearsBetween(
                    LocalDate.of(2023, 1, 1), LocalDate.of(2026, 1, 1)));
        }
    }

    // ==================== Query ====================

    @Nested
    @DisplayName("Query methods")
    class Query {

        @Test
        @DisplayName("now returns today's date")
        void now() {
            assertEquals(LocalDate.now(), TaxDateUtils.now());
        }

        @Test
        @DisplayName("currentYear returns the current year")
        void currentYear() {
            assertEquals(LocalDate.now().getYear(), TaxDateUtils.currentYear());
        }

        @Test
        @DisplayName("currentMonth returns the current month")
        void currentMonth() {
            assertEquals(LocalDate.now().getMonthValue(), TaxDateUtils.currentMonth());
        }

        @Test
        @DisplayName("getQuarter maps months to quarters correctly")
        void getQuarter() {
            assertEquals(1, TaxDateUtils.getQuarter(1));
            assertEquals(1, TaxDateUtils.getQuarter(3));
            assertEquals(2, TaxDateUtils.getQuarter(4));
            assertEquals(2, TaxDateUtils.getQuarter(6));
            assertEquals(3, TaxDateUtils.getQuarter(7));
            assertEquals(3, TaxDateUtils.getQuarter(9));
            assertEquals(4, TaxDateUtils.getQuarter(10));
            assertEquals(4, TaxDateUtils.getQuarter(12));
        }

        @Test
        @DisplayName("getQuarter throws for invalid month")
        void getQuarter_invalid() {
            assertThrows(IllegalArgumentException.class, () -> TaxDateUtils.getQuarter(0));
            assertThrows(IllegalArgumentException.class, () -> TaxDateUtils.getQuarter(13));
        }

        @Test
        @DisplayName("getMonthsBetween lists all months in range")
        void getMonthsBetween_localDate() {
            List<String> months = TaxDateUtils.getMonthsBetween(
                    LocalDate.of(2026, 1, 15), LocalDate.of(2026, 4, 10));
            assertEquals(List.of("202601", "202602", "202603", "202604"), months);
        }

        @Test
        @DisplayName("getMonthsBetween with string arguments")
        void getMonthsBetween_strings() {
            List<String> months = TaxDateUtils.getMonthsBetween("202601", "202603");
            assertEquals(List.of("202601", "202602", "202603"), months);
        }

        @Test
        @DisplayName("getMonthsBetween throws if start is after end")
        void getMonthsBetween_invalidRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDateUtils.getMonthsBetween(
                            LocalDate.of(2026, 6, 1), LocalDate.of(2026, 1, 1)));
        }

        @Test
        @DisplayName("getRecentMonths returns the correct number of recent months")
        void getRecentMonths() {
            List<String> months = TaxDateUtils.getRecentMonths(3);
            assertEquals(3, months.size());
            // All should be in yyyy-MM format
            for (String m : months) {
                assertTrue(m.matches("\\d{4}-\\d{2}"));
            }
        }

        @Test
        @DisplayName("getRecentMonths returns empty list for count <= 0")
        void getRecentMonths_zeroOrNegative() {
            assertTrue(TaxDateUtils.getRecentMonths(0).isEmpty());
            assertTrue(TaxDateUtils.getRecentMonths(-1).isEmpty());
        }

        @Test
        @DisplayName("getMonthsOfYear returns all 12 months")
        void getMonthsOfYear() {
            List<String> months = TaxDateUtils.getMonthsOfYear(2026);
            assertEquals(12, months.size());
            assertEquals("202601", months.get(0));
            assertEquals("202612", months.get(11));
        }

        @Test
        @DisplayName("getQuartersOfYear returns all 4 quarters")
        void getQuartersOfYear() {
            List<String> quarters = TaxDateUtils.getQuartersOfYear(2026);
            assertEquals(List.of("2026Q1", "2026Q2", "2026Q3", "2026Q4"), quarters);
        }

        @Test
        @DisplayName("getQuarterEndDates returns quarter boundaries within range")
        void getQuarterEndDates() {
            List<LocalDate> ends = TaxDateUtils.getQuarterEndDates("2026-01-01", "2026-12-31");
            assertEquals(4, ends.size());
            assertEquals(LocalDate.of(2026, 3, 31), ends.get(0));
            assertEquals(LocalDate.of(2026, 6, 30), ends.get(1));
            assertEquals(LocalDate.of(2026, 9, 30), ends.get(2));
            assertEquals(LocalDate.of(2026, 12, 31), ends.get(3));
        }

        @Test
        @DisplayName("getQuarterEndDates handles partial range")
        void getQuarterEndDates_partialRange() {
            List<LocalDate> ends = TaxDateUtils.getQuarterEndDates("2026-02-01", "2026-08-31");
            assertEquals(2, ends.size());
            assertEquals(LocalDate.of(2026, 3, 31), ends.get(0));
            assertEquals(LocalDate.of(2026, 6, 30), ends.get(1));
        }

        @Test
        @DisplayName("getQuarterEndDates throws if start is after end")
        void getQuarterEndDates_invalidRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> TaxDateUtils.getQuarterEndDates("2026-12-31", "2026-01-01"));
        }
    }

    // ==================== Pattern Constants ====================

    @Nested
    @DisplayName("Pattern constants")
    class PatternConstants {

        @Test
        @DisplayName("all pattern constants have expected values")
        void patternValues() {
            assertEquals("yyyy年MM月dd日 HH:mm:ss", TaxDateUtils.PATTERN_DATETIME_CN);
            assertEquals("yyyy.MM.dd.HH:mm:ss", TaxDateUtils.PATTERN_DATETIME_DOT);
            assertEquals("yyyy/MM/dd", TaxDateUtils.PATTERN_DATE_SLASH);
            assertEquals("yyyyMM", TaxDateUtils.PATTERN_MONTH);
            assertEquals("yyyy-MM", TaxDateUtils.PATTERN_MONTH_DASH);
            assertEquals("yyyy-MM-dd", TaxDateUtils.PATTERN_DATE);
            assertEquals("yyyyMMdd", TaxDateUtils.PATTERN_DATE_COMPACT);
        }
    }
}
