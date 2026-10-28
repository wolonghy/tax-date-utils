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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * General-purpose date utility methods for tax and financial systems.
 *
 * <p>This class provides static helpers for:</p>
 * <ul>
 *   <li><strong>Conversion</strong> — between {@link LocalDate}, {@link LocalDateTime}, and epoch millis
 *       (e.g. {@link #toLocalDate(long)}, {@link #toLocalDateTime(LocalDate)})</li>
 *   <li><strong>Formatting</strong> — dates to strings with common patterns
 *       (e.g. {@link #formatDate(LocalDate)}, {@link #formatMonthDash(LocalDateTime)})</li>
 *   <li><strong>Parsing</strong> — strings to dates with error handling
 *       (e.g. {@link #parseLocalDate(String)}, {@link #parseMonth(String)})</li>
 *   <li><strong>Calculation</strong> — offsets, first/last day of month/quarter/half-year
 *       (e.g. {@link #offsetMonths(LocalDate, int)}, {@link #getLastDayOfQuarter(int, int)})</li>
 *   <li><strong>Judgment</strong> — leap year, containment, overlap, validity checks
 *       (e.g. {@link #isLeapYear(int)}, {@link #isBetween(LocalDate, LocalDate, LocalDate)})</li>
 *   <li><strong>Difference</strong> — days, months, years between dates
 *       (e.g. {@link #daysBetween(LocalDate, LocalDate)}, {@link #monthsBetween(LocalDate, LocalDate)})</li>
 *   <li><strong>Query</strong> — current date, quarter from month, month enumeration
 *       (e.g. {@link #now()}, {@link #getQuarter(int)}, {@link #getMonthsBetween(LocalDate, LocalDate)})</li>
 * </ul>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class TaxDateUtils {

    private TaxDateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Date Format Patterns ====================

    /** Chinese datetime pattern: {@code "yyyy年MM月dd日 HH:mm:ss"}. */
    public static final String PATTERN_DATETIME_CN = "yyyy年MM月dd日 HH:mm:ss";
    /** Chinese full datetime pattern: {@code "yyyy年MM月dd日 HH时mm分ss秒"}. */
    public static final String PATTERN_DATETIME_CN_FULL = "yyyy年MM月dd日 HH时mm分ss秒";
    /** Dot-separated datetime pattern: {@code "yyyy.MM.dd.HH:mm:ss"}. */
    public static final String PATTERN_DATETIME_DOT = "yyyy.MM.dd.HH:mm:ss";
    /** Slash-separated date pattern: {@code "yyyy/MM/dd"}. */
    public static final String PATTERN_DATE_SLASH = "yyyy/MM/dd";
    /** Compact month pattern: {@code "yyyyMM"}. */
    public static final String PATTERN_MONTH = "yyyyMM";
    /** Dash-separated month pattern: {@code "yyyy-MM"}. */
    public static final String PATTERN_MONTH_DASH = "yyyy-MM";
    /** ISO date pattern: {@code "yyyy-MM-dd"}. */
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    /** Compact date pattern: {@code "yyyyMMdd"}. */
    public static final String PATTERN_DATE_COMPACT = "yyyyMMdd";
    /** Full datetime pattern: {@code "yyyy-MM-dd HH:mm:ss"}. */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    /** Millisecond timestamp pattern: {@code "yyyyMMddHHmmssSSS"}. */
    public static final String PATTERN_DATETIME_MS = "yyyyMMddHHmmssSSS";
    /** Datetime-second timestamp pattern: {@code "yyyyMMddHHmmss"}. */
    public static final String PATTERN_DATETIME_COMPACT = "yyyyMMddHHmmss";
    /** Dot-separated date pattern: {@code "yyyy.MM.dd"}. */
    public static final String PATTERN_DATE_DOT = "yyyy.MM.dd";
    /** Short year date pattern: {@code "yyMMdd"}. */
    public static final String PATTERN_DATE_SHORT = "yyMMdd";
    /** Datetime-to-minute pattern: {@code "yyyy-MM-dd HH:mm"}. */
    public static final String PATTERN_DATETIME_MINUTE = "yyyy-MM-dd HH:mm";
    /** Year-only pattern: {@code "yyyy"}. */
    public static final String PATTERN_YEAR = "yyyy";

    // ==================== Cached Formatters ====================

    private static final DateTimeFormatter FMT_DATETIME_CN = DateTimeFormatter.ofPattern(PATTERN_DATETIME_CN);
    private static final DateTimeFormatter FMT_DATETIME_CN_FULL = DateTimeFormatter.ofPattern(PATTERN_DATETIME_CN_FULL);
    private static final DateTimeFormatter FMT_DATETIME_DOT = DateTimeFormatter.ofPattern(PATTERN_DATETIME_DOT);
    private static final DateTimeFormatter FMT_DATE_SLASH = DateTimeFormatter.ofPattern(PATTERN_DATE_SLASH);
    private static final DateTimeFormatter FMT_MONTH = DateTimeFormatter.ofPattern(PATTERN_MONTH);
    private static final DateTimeFormatter FMT_MONTH_DASH = DateTimeFormatter.ofPattern(PATTERN_MONTH_DASH);
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);
    private static final DateTimeFormatter FMT_DATE_COMPACT = DateTimeFormatter.ofPattern(PATTERN_DATE_COMPACT);
    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern(PATTERN_DATETIME);
    private static final DateTimeFormatter FMT_DATETIME_MS = DateTimeFormatter.ofPattern(PATTERN_DATETIME_MS);
    private static final DateTimeFormatter FMT_DATETIME_COMPACT = DateTimeFormatter.ofPattern(PATTERN_DATETIME_COMPACT);
    private static final DateTimeFormatter FMT_DATE_DOT = DateTimeFormatter.ofPattern(PATTERN_DATE_DOT);
    private static final DateTimeFormatter FMT_DATE_SHORT = DateTimeFormatter.ofPattern(PATTERN_DATE_SHORT);
    private static final DateTimeFormatter FMT_DATETIME_MINUTE = DateTimeFormatter.ofPattern(PATTERN_DATETIME_MINUTE);
    private static final DateTimeFormatter FMT_YEAR = DateTimeFormatter.ofPattern(PATTERN_YEAR);

    // ==================== Conversion Methods ====================

    /**
     * Converts epoch milliseconds to a {@link LocalDate} using the system default time zone.
     *
     * @param epochMilli milliseconds since the Unix epoch
     * @return the corresponding {@code LocalDate}
     */
    public static LocalDate toLocalDate(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts a {@link LocalDate} to a {@link LocalDateTime} at the start of day ({@code 00:00:00}).
     *
     * @param date the date to convert, never {@code null}
     * @return the corresponding {@code LocalDateTime} at midnight
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public static LocalDateTime toLocalDateTime(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.atStartOfDay();
    }

    /**
     * Converts a {@link LocalDate} to a {@link LocalDateTime} at the specified time.
     *
     * @param date   the date to convert, never {@code null}
     * @param hour   the hour-of-day (0–23)
     * @param minute the minute-of-hour (0–59)
     * @param second the second-of-minute (0–59)
     * @return the corresponding {@code LocalDateTime}
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public static LocalDateTime toLocalDateTime(LocalDate date, int hour, int minute, int second) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.atTime(hour, minute, second);
    }

    /**
     * Extracts the {@link LocalDate} part from a {@link LocalDateTime}.
     *
     * @param dateTime the datetime to convert, never {@code null}
     * @return the date portion of the datetime
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.toLocalDate();
    }

    // ==================== Component Extraction — LocalDate ====================

    /**
     * Extracts the 4-digit year from a {@link LocalDate} (e.g. {@code "2026"}).
     *
     * @param date the date, never {@code null}
     * @return the year as a 4-digit string
     */
    public static String getYearStr(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return String.format("%04d", date.getYear());
    }

    /**
     * Extracts the 2-digit month from a {@link LocalDate} (e.g. {@code "01"} for January).
     *
     * @param date the date, never {@code null}
     * @return the month as a zero-padded 2-digit string
     */
    public static String getMonthStr(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return String.format("%02d", date.getMonthValue());
    }

    /**
     * Extracts the 2-digit day from a {@link LocalDate} (e.g. {@code "05"} or {@code "15"}).
     *
     * @param date the date, never {@code null}
     * @return the day as a zero-padded 2-digit string
     */
    public static String getDayStr(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return String.format("%02d", date.getDayOfMonth());
    }

    // ==================== Component Extraction — LocalDateTime ====================

    /**
     * Extracts the 4-digit year from a {@link LocalDateTime} (e.g. {@code "2026"}).
     *
     * @param dateTime the datetime, never {@code null}
     * @return the year as a 4-digit string
     */
    public static String getYearStr(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return String.format("%04d", dateTime.getYear());
    }

    /**
     * Extracts the 2-digit month from a {@link LocalDateTime} (e.g. {@code "01"} for January).
     *
     * @param dateTime the datetime, never {@code null}
     * @return the month as a zero-padded 2-digit string
     */
    public static String getMonthStr(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return String.format("%02d", dateTime.getMonthValue());
    }

    /**
     * Extracts the 2-digit day from a {@link LocalDateTime} (e.g. {@code "05"} or {@code "15"}).
     *
     * @param dateTime the datetime, never {@code null}
     * @return the day as a zero-padded 2-digit string
     */
    public static String getDayStr(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return String.format("%02d", dateTime.getDayOfMonth());
    }

    /**
     * Extracts the 2-digit hour from a {@link LocalDateTime} (e.g. {@code "08"} or {@code "14"}).
     *
     * @param dateTime the datetime, never {@code null}
     * @return the hour as a zero-padded 2-digit string (00-23)
     */
    public static String getHourStr(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return String.format("%02d", dateTime.getHour());
    }

    /**
     * Extracts the 2-digit minute from a {@link LocalDateTime} (e.g. {@code "05"} or {@code "30"}).
     *
     * @param dateTime the datetime, never {@code null}
     * @return the minute as a zero-padded 2-digit string (00-59)
     */
    public static String getMinuteStr(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return String.format("%02d", dateTime.getMinute());
    }

    /**
     * Extracts the 2-digit second from a {@link LocalDateTime} (e.g. {@code "05"} or {@code "45"}).
     *
     * @param dateTime the datetime, never {@code null}
     * @return the second as a zero-padded 2-digit string (00-59)
     */
    public static String getSecondStr(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return String.format("%02d", dateTime.getSecond());
    }

    // ==================== Formatting Methods ====================

    /**
     * Formats a {@link LocalDate} using the given pattern.
     *
     * @param date    the date to format, never {@code null}
     * @param pattern the date format pattern, never {@code null}
     * @return the formatted date string
     * @throws NullPointerException if any argument is {@code null}
     */
    public static String format(LocalDate date, String pattern) {
        Objects.requireNonNull(date, "Date must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a {@link LocalDateTime} using the given pattern.
     *
     * @param dateTime the datetime to format, never {@code null}
     * @param pattern  the datetime format pattern, never {@code null}
     * @return the formatted datetime string
     * @throws NullPointerException if any argument is {@code null}
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== Formatting Methods — LocalDate ====================

    /**
     * Formats a {@link LocalDate} as {@code "yyyy-MM-dd"} (e.g. {@code "2026-01-15"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted date string
     */
    public static String formatDate(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_DATE);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyyyMM"} (e.g. {@code "202601"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted month string
     */
    public static String formatMonth(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_MONTH);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyyy-MM"} (e.g. {@code "2026-01"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted month string
     */
    public static String formatMonthDash(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_MONTH_DASH);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyyyMMdd"} (e.g. {@code "20260115"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted compact date string
     */
    public static String formatDateCompact(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_DATE_COMPACT);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyyy/MM/dd"} (e.g. {@code "2026/01/15"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted slash date string
     */
    public static String formatDateSlash(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_DATE_SLASH);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyyy.MM.dd"} (e.g. {@code "2026.01.15"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted dot date string
     */
    public static String formatDateDot(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_DATE_DOT);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyMMdd"} (e.g. {@code "260115"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted short date string
     */
    public static String formatDateShort(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_DATE_SHORT);
    }

    /**
     * Formats a {@link LocalDate} as {@code "yyyy"} (e.g. {@code "2026"}).
     *
     * @param date the date to format, never {@code null}
     * @return the formatted year string
     */
    public static String formatYear(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.format(FMT_YEAR);
    }

    // ==================== Formatting Methods — LocalDateTime ====================

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy-MM-dd"} (e.g. {@code "2026-01-15"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted date string
     */
    public static String formatDate(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATE);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyyMM"} (e.g. {@code "202601"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted month string
     */
    public static String formatMonth(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_MONTH);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy-MM"} (e.g. {@code "2026-01"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted month string
     */
    public static String formatMonthDash(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_MONTH_DASH);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyyMMdd"} (e.g. {@code "20260115"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted compact date string
     */
    public static String formatDateCompact(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATE_COMPACT);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy/MM/dd"} (e.g. {@code "2026/01/15"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted slash date string
     */
    public static String formatDateSlash(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATE_SLASH);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy.MM.dd"} (e.g. {@code "2026.01.15"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted dot date string
     */
    public static String formatDateDot(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATE_DOT);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyMMdd"} (e.g. {@code "260115"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted short date string
     */
    public static String formatDateShort(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATE_SHORT);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy-MM-dd HH:mm:ss"} (e.g. {@code "2026-01-15 14:30:00"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyyMMddHHmmss"} (e.g. {@code "20260115143000"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted compact datetime string
     */
    public static String formatDateTimeCompact(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME_COMPACT);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyyMMddHHmmssSSS"} (e.g. {@code "20260115143000123"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted millisecond datetime string
     */
    public static String formatDateTimeMs(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME_MS);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy年MM月dd日 HH:mm:ss"} (e.g. {@code "2026年01月15日 14:30:00"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted Chinese datetime string
     */
    public static String formatDateTimeCn(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME_CN);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy年MM月dd日 HH时mm分ss秒"}
     * (e.g. {@code "2026年01月15日 14时30分45秒"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted Chinese full datetime string
     */
    public static String formatDateTimeCnFull(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME_CN_FULL);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy.MM.dd.HH:mm:ss"} (e.g. {@code "2026.01.15.14:30:00"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted dot datetime string
     */
    public static String formatDateTimeDot(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME_DOT);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy-MM-dd HH:mm"} (e.g. {@code "2026-01-15 14:30"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted datetime-to-minute string
     */
    public static String formatDateTimeMinute(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_DATETIME_MINUTE);
    }

    /**
     * Formats a {@link LocalDateTime} as {@code "yyyy"} (e.g. {@code "2026"}).
     *
     * @param dateTime the datetime to format, never {@code null}
     * @return the formatted year string
     */
    public static String formatYear(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        return dateTime.format(FMT_YEAR);
    }

    // ==================== Parsing Methods ====================

    /**
     * Parses a date string using the given pattern.
     *
     * @param dateStr the date string to parse, never {@code null}
     * @param pattern the expected format pattern, never {@code null}
     * @return the parsed {@code LocalDate}
     * @throws IllegalArgumentException if the string cannot be parsed
     * @throws NullPointerException     if any argument is {@code null}
     */
    public static LocalDate parseLocalDate(String dateStr, String pattern) {
        Objects.requireNonNull(dateStr, "Date string must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Failed to parse date: '" + dateStr + "' with pattern: '" + pattern + "'", e);
        }
    }

    /**
     * Parses a date string in {@code "yyyy-MM-dd"} format.
     *
     * @param dateStr the date string (e.g. {@code "2026-01-15"})
     * @return the parsed {@code LocalDate}
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static LocalDate parseLocalDate(String dateStr) {
        return parseLocalDate(dateStr, PATTERN_DATE);
    }

    /**
     * Parses a month string in {@code "yyyyMM"} format and returns the first day of that month.
     *
     * @param monthStr the month string (e.g. {@code "202601"})
     * @return the {@code LocalDate} representing the first day of the month
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static LocalDate parseMonth(String monthStr) {
        Objects.requireNonNull(monthStr, "Month string must not be null");
        try {
            return YearMonth.parse(monthStr, DateTimeFormatter.ofPattern(PATTERN_MONTH))
                    .atDay(1);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Failed to parse month: '" + monthStr + "' with pattern: '" + PATTERN_MONTH + "'", e);
        }
    }

    /**
     * Parses a datetime string using the given pattern.
     *
     * @param dateTimeStr the datetime string to parse, never {@code null}
     * @param pattern     the expected format pattern, never {@code null}
     * @return the parsed {@code LocalDateTime}
     * @throws IllegalArgumentException if the string cannot be parsed
     * @throws NullPointerException     if any argument is {@code null}
     */
    public static LocalDateTime parseLocalDateTime(String dateTimeStr, String pattern) {
        Objects.requireNonNull(dateTimeStr, "DateTime string must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Failed to parse datetime: '" + dateTimeStr + "' with pattern: '" + pattern + "'", e);
        }
    }

    // ==================== Date Calculation Methods ====================

    /**
     * Offsets a date by the specified amount and unit.
     *
     * @param date   the base date, never {@code null}
     * @param unit   the temporal unit (e.g. {@link ChronoUnit#DAYS}), never {@code null}
     * @param amount the amount to add (may be negative)
     * @return the offset date
     */
    public static LocalDate offset(LocalDate date, ChronoUnit unit, long amount) {
        Objects.requireNonNull(date, "Date must not be null");
        Objects.requireNonNull(unit, "ChronoUnit must not be null");
        return date.plus(amount, unit);
    }

    /**
     * Offsets a date by the specified number of years.
     *
     * @param date  the base date
     * @param years the number of years to add (may be negative)
     * @return the offset date
     */
    public static LocalDate offsetYears(LocalDate date, int years) {
        return offset(date, ChronoUnit.YEARS, years);
    }

    /**
     * Offsets a date by the specified number of months.
     *
     * @param date   the base date
     * @param months the number of months to add (may be negative)
     * @return the offset date
     */
    public static LocalDate offsetMonths(LocalDate date, int months) {
        return offset(date, ChronoUnit.MONTHS, months);
    }

    /**
     * Offsets a date by the specified number of days.
     *
     * @param date the base date
     * @param days the number of days to add (may be negative)
     * @return the offset date
     */
    public static LocalDate offsetDays(LocalDate date, int days) {
        return offset(date, ChronoUnit.DAYS, days);
    }

    /**
     * Returns the first day of the specified month.
     *
     * @param year  the calendar year
     * @param month the month (1–12)
     * @return the first day of the month
     */
    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /**
     * Returns the last day of the specified month.
     *
     * @param year  the calendar year
     * @param month the month (1–12)
     * @return the last day of the month (handles leap years correctly)
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Returns the last day of the specified month as a formatted string ({@code "yyyy-MM-dd"}).
     *
     * @param year  the calendar year
     * @param month the month (1–12)
     * @return the formatted last-day string
     */
    public static String getLastDayOfMonthStr(int year, int month) {
        return formatDate(getLastDayOfMonth(year, month));
    }

    /**
     * Returns the first day of the specified quarter.
     *
     * <p>Q1 starts in January, Q2 in April, Q3 in July, Q4 in October.</p>
     *
     * @param year    the calendar year
     * @param quarter the quarter (1–4)
     * @return the first day of the quarter
     * @throws IllegalArgumentException if {@code quarter} is not between 1 and 4
     */
    public static LocalDate getFirstDayOfQuarter(int year, int quarter) {
        validateQuarter(quarter);
        // Q1→month 1, Q2→month 4, Q3→month 7, Q4→month 10
        return LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
    }

    /**
     * Returns the last day of the specified quarter.
     *
     * @param year    the calendar year
     * @param quarter the quarter (1–4)
     * @return the last day of the quarter's final month
     * @throws IllegalArgumentException if {@code quarter} is not between 1 and 4
     */
    public static LocalDate getLastDayOfQuarter(int year, int quarter) {
        validateQuarter(quarter);
        // Q1→Mar, Q2→Jun, Q3→Sep, Q4→Dec
        return LocalDate.of(year, quarter * 3, 1).with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Returns the first day of the first half of the year (January 1).
     *
     * @param year the calendar year
     * @return {@code January 1} of the given year
     */
    public static LocalDate getFirstDayOfFirstHalf(int year) {
        return LocalDate.of(year, 1, 1);
    }

    /**
     * Returns the last day of the first half of the year (June 30).
     *
     * @param year the calendar year
     * @return {@code June 30} of the given year
     */
    public static LocalDate getLastDayOfFirstHalf(int year) {
        return LocalDate.of(year, 6, 30);
    }

    /**
     * Returns the first day of the second half of the year (July 1).
     *
     * @param year the calendar year
     * @return {@code July 1} of the given year
     */
    public static LocalDate getFirstDayOfSecondHalf(int year) {
        return LocalDate.of(year, 7, 1);
    }

    /**
     * Returns the last day of the second half of the year (December 31).
     *
     * @param year the calendar year
     * @return {@code December 31} of the given year
     */
    public static LocalDate getLastDayOfSecondHalf(int year) {
        return LocalDate.of(year, 12, 31);
    }

    // ==================== Date Judgment Methods ====================

    /**
     * Checks whether the given year is a leap year.
     *
     * <p>A year is a leap year if it is divisible by 4 but not by 100,
     * unless it is also divisible by 400.</p>
     *
     * @param year the year to check
     * @return {@code true} if the year is a leap year
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Checks whether a date falls within a range (inclusive on both ends).
     *
     * @param date  the date to test, never {@code null}
     * @param start the range start (inclusive), never {@code null}
     * @param end   the range end (inclusive), never {@code null}
     * @return {@code true} if {@code start <= date <= end}
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        Objects.requireNonNull(date, "Date must not be null");
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Checks whether two date ranges overlap.
     *
     * @param start1 start of the first range
     * @param end1   end of the first range
     * @param start2 start of the second range
     * @param end2   end of the second range
     * @return {@code true} if the ranges share at least one common date
     */
    public static boolean isOverlap(LocalDate start1, LocalDate end1,
                                    LocalDate start2, LocalDate end2) {
        Objects.requireNonNull(start1, "Start date must not be null");
        Objects.requireNonNull(end1, "End date must not be null");
        Objects.requireNonNull(start2, "Start date must not be null");
        Objects.requireNonNull(end2, "End date must not be null");
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    /**
     * Checks whether two date ranges (given as {@code "yyyy-MM-dd"} strings) overlap.
     *
     * @param start1 start of the first range
     * @param end1   end of the first range
     * @param start2 start of the second range
     * @param end2   end of the second range
     * @return {@code true} if the ranges overlap
     */
    public static boolean isOverlap(String start1, String end1,
                                    String start2, String end2) {
        return isOverlap(
                parseLocalDate(start1), parseLocalDate(end1),
                parseLocalDate(start2), parseLocalDate(end2));
    }

    /**
     * Checks whether a date string is valid for the given pattern.
     *
     * @param dateStr the date string to validate
     * @param pattern the expected format pattern
     * @return {@code true} if the string can be parsed with the pattern
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isBlank() || pattern == null || pattern.isBlank()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate parsed = LocalDate.parse(dateStr, formatter);
            return parsed.format(formatter).equals(dateStr);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks whether a date string is valid in {@code "yyyy-MM-dd"} format.
     *
     * @param dateStr the date string to validate
     * @return {@code true} if the string is a valid ISO date
     */
    public static boolean isValidDate(String dateStr) {
        return isValidDate(dateStr, PATTERN_DATE);
    }

    // ==================== Date Difference Methods ====================

    /**
     * Returns the number of days between two dates.
     *
     * @param start the start date
     * @param end   the end date
     * @return the number of days from {@code start} to {@code end}
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Returns the number of days between two dates given as {@code "yyyy-MM-dd"} strings.
     *
     * @param start the start date string
     * @param end   the end date string
     * @return the number of days between the two dates
     */
    public static long daysBetween(String start, String end) {
        return daysBetween(parseLocalDate(start), parseLocalDate(end));
    }

    /**
     * Returns the number of months between two dates.
     *
     * @param start the start date
     * @param end   the end date
     * @return the number of months from {@code start} to {@code end}
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Returns the number of years between two dates.
     *
     * @param start the start date
     * @param end   the end date
     * @return the number of years from {@code start} to {@code end}
     */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        return ChronoUnit.YEARS.between(start, end);
    }

    // ==================== Query Methods ====================

    /**
     * Returns today's date.
     *
     * @return the current {@link LocalDate}
     */
    public static LocalDate now() {
        return LocalDate.now();
    }

    /**
     * Returns the current year.
     *
     * @return the current year (e.g. {@code 2026})
     */
    public static int currentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * Returns the current month (1–12).
     *
     * @return the current month
     */
    public static int currentMonth() {
        return LocalDate.now().getMonthValue();
    }

    /**
     * Returns the quarter that the given month belongs to.
     *
     * <p>Months 1–3 → Q1, 4–6 → Q2, 7–9 → Q3, 10–12 → Q4.</p>
     *
     * @param month the month (1–12)
     * @return the quarter number (1–4)
     * @throws IllegalArgumentException if {@code month} is not between 1 and 12
     */
    public static int getQuarter(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return (month - 1) / 3 + 1;
    }

    /**
     * Returns a list of month strings ({@code "yyyyMM"}) between two dates (inclusive).
     *
     * @param start the start date
     * @param end   the end date
     * @return a list of month strings in {@code "yyyyMM"} format
     * @throws IllegalArgumentException if {@code start} is after {@code end}
     */
    public static List<String> getMonthsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        List<String> months = new ArrayList<>();
        // Normalize to the 1st of the start month
        LocalDate current = start.withDayOfMonth(1);
        while (!current.isAfter(end)) {
            months.add(current.format(FMT_MONTH));
            current = current.plusMonths(1);
        }
        return months;
    }

    /**
     * Returns a list of month strings between two month strings (inclusive).
     *
     * @param startStr the start month in {@code "yyyyMM"} format
     * @param endStr   the end month in {@code "yyyyMM"} format
     * @return a list of month strings in {@code "yyyyMM"} format
     */
    public static List<String> getMonthsBetween(String startStr, String endStr) {
        return getMonthsBetween(parseMonth(startStr), parseMonth(endStr));
    }

    /**
     * Returns the most recent N months as dash-formatted strings ({@code "yyyy-MM"}),
     * ordered from most recent to oldest.
     *
     * @param count the number of months to return
     * @return a list of recent month strings; empty if {@code count <= 0}
     */
    public static List<String> getRecentMonths(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<String> months = new ArrayList<>(count);
        LocalDate now = LocalDate.now();
        for (int i = 1; i <= count; i++) {
            months.add(now.minusMonths(i).format(FMT_MONTH_DASH));
        }
        return months;
    }

    /**
     * Returns all 12 months of the given year as {@code "yyyyMM"} strings.
     *
     * @param year the calendar year
     * @return a list of 12 month strings from {@code "01"} to {@code "12"}
     */
    public static List<String> getMonthsOfYear(int year) {
        List<String> months = new ArrayList<>(12);
        for (int i = 1; i <= 12; i++) {
            months.add(year + String.format("%02d", i));
        }
        return months;
    }

    /**
     * Returns all 4 quarters of the given year as strings (e.g. {@code "2026Q1"} … {@code "2026Q4"}).
     *
     * @param year the calendar year
     * @return a list of 4 quarter strings
     */
    public static List<String> getQuartersOfYear(int year) {
        return List.of(year + "Q1", year + "Q2", year + "Q3", year + "Q4");
    }

    /**
     * Returns the last day of each quarter that falls within the given date range.
     *
     * <p>Quarter end dates are: Mar 31, Jun 30, Sep 30, Dec 31.</p>
     *
     * @param startStr the range start in {@code "yyyy-MM-dd"} format
     * @param endStr   the range end in {@code "yyyy-MM-dd"} format
     * @return a list of quarter-end dates within the range
     * @throws IllegalArgumentException if {@code start} is after {@code end}
     */
    public static List<LocalDate> getQuarterEndDates(String startStr, String endStr) {
        LocalDate start = parseLocalDate(startStr);
        LocalDate end = parseLocalDate(endStr);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        List<LocalDate> quarterEnds = new ArrayList<>();
        LocalDate current = start.withDayOfMonth(1);
        while (!current.isAfter(end)) {
            int m = current.getMonthValue();
            // Quarter end months: March(3), June(6), September(9), December(12)
            if (m == 3 || m == 6 || m == 9 || m == 12) {
                LocalDate quarterEnd = current.with(TemporalAdjusters.lastDayOfMonth());
                if (!quarterEnd.isBefore(start) && !quarterEnd.isAfter(end)) {
                    quarterEnds.add(quarterEnd);
                }
            }
            current = current.plusMonths(1);
        }
        return quarterEnds;
    }

    // ==================== Timezone-Aware Methods ====================

    /**
     * Returns today's date in the specified time zone.
     *
     * @param zone the time zone, never {@code null}
     * @return the current {@link LocalDate} in the given zone
     */
    public static LocalDate now(ZoneId zone) {
        Objects.requireNonNull(zone, "ZoneId must not be null");
        return LocalDate.now(zone);
    }

    /**
     * Returns the current date-time in the specified time zone.
     *
     * @param zone the time zone, never {@code null}
     * @return the current {@link LocalDateTime} in the given zone
     */
    public static LocalDateTime nowDateTime(ZoneId zone) {
        Objects.requireNonNull(zone, "ZoneId must not be null");
        return LocalDateTime.now(zone);
    }

    // ==================== Additional Utility Methods ====================

    /**
     * Returns the last day of the specified month as a formatted string.
     *
     * @param year    the calendar year
     * @param month   the month (1–12)
     * @param pattern the output format pattern
     * @return the formatted last-day string
     */
    public static String getLastDayOfMonthStr(int year, int month, String pattern) {
        Objects.requireNonNull(pattern, "Pattern must not be null");
        return format(getLastDayOfMonth(year, month), pattern);
    }

    /**
     * Returns a list of month strings ({@code "yyyy-MM"}) between two dates (inclusive).
     *
     * @param start the start date
     * @param end   the end date
     * @return a list of month strings in {@code "yyyy-MM"} format
     */
    public static List<String> getMonthsBetweenDash(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        List<String> months = new ArrayList<>();
        LocalDate current = start.withDayOfMonth(1);
        while (!current.isAfter(end)) {
            months.add(current.format(FMT_MONTH_DASH));
            current = current.plusMonths(1);
        }
        return months;
    }

    /**
     * Returns the most recent N days as {@code "yyyy-MM-dd"} strings,
     * ordered from most recent to oldest.
     *
     * @param count the number of days to return
     * @return a list of recent date strings; empty if {@code count <= 0}
     */
    public static List<String> getRecentDays(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<String> days = new ArrayList<>(count);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            days.add(today.minusDays(i).format(FMT_DATE));
        }
        return days;
    }

    /**
     * Returns the most recent N days as {@code "yyyy-MM-dd"} strings,
     * ordered from most recent to oldest, using the specified pattern.
     *
     * @param count   the number of days to return
     * @param pattern the output format pattern
     * @return a list of recent date strings
     */
    public static List<String> getRecentDays(int count, String pattern) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        Objects.requireNonNull(pattern, "Pattern must not be null");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        List<String> days = new ArrayList<>(count);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            days.add(today.minusDays(i).format(fmt));
        }
        return days;
    }

    /**
     * Validates a date string and returns a detailed error message if invalid.
     *
     * @param dateStr the date string to validate
     * @param pattern the expected format pattern
     * @return {@code null} if valid, or an error message describing why the date is invalid
     */
    public static String validateDate(String dateStr, String pattern) {
        if (dateStr == null) {
            return "Date string is null";
        }
        if (dateStr.isBlank()) {
            return "Date string is blank";
        }
        if (pattern == null || pattern.isBlank()) {
            return "Pattern is null or blank";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate parsed = LocalDate.parse(dateStr, formatter);
            String roundTrip = parsed.format(formatter);
            if (!roundTrip.equals(dateStr)) {
                return "Date '" + dateStr + "' does not match pattern '" + pattern
                        + "' (parsed as '" + roundTrip + "')";
            }
            return null;
        } catch (DateTimeParseException e) {
            return "Failed to parse date '" + dateStr + "' with pattern '" + pattern + "': " + e.getMessage();
        }
    }

    /**
     * Generates a timestamp string using the {@link #PATTERN_DATETIME_COMPACT} pattern
     * ({@code "yyyyMMddHHmmss"}), suitable for export filenames.
     *
     * @return the current timestamp string
     */
    public static String nowTimestamp() {
        return LocalDateTime.now().format(FMT_DATETIME_COMPACT);
    }

    /**
     * Generates a timestamp string using the {@link #PATTERN_DATETIME_MS} pattern
     * ({@code "yyyyMMddHHmmssSSS"}), suitable for unique ID generation.
     *
     * @return the current millisecond-precision timestamp string
     */
    public static String nowTimestampMs() {
        return LocalDateTime.now().format(FMT_DATETIME_MS);
    }

    /**
     * Generates a unique timestamp-based ID by combining a millisecond timestamp with a random suffix.
     *
     * @param randomDigits the number of random digits to append
     * @return a unique ID string (e.g. {@code "20261028153045123"} + random)
     */
    public static String generateTimestampId(int randomDigits) {
        if (randomDigits < 1) {
            throw new IllegalArgumentException("Random digits must be at least 1");
        }
        StringBuilder sb = new StringBuilder(nowTimestampMs());
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < randomDigits; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    // ==================== Private Helpers ====================

    private static void validateQuarter(int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }
    }
}
