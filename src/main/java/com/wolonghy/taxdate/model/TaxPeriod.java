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

import com.wolonghy.taxdate.constant.PeriodConstants;
import com.wolonghy.taxdate.constant.PeriodType;
import com.wolonghy.taxdate.exception.TaxDateException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * The core domain model representing a tax period.
 *
 * <p>A {@code TaxPeriod} is an immutable {@link java.lang.Record record} that
 * encapsulates the string {@linkplain #value() value}, the {@linkplain #type() period type},
 * and the extracted components (year, quarter, month, correction sequence).</p>
 *
 * <h3>Supported formats</h3>
 * <table>
 *   <caption>Period format examples</caption>
 *   <tr><th>Format</th><th>Example</th><th>Type</th></tr>
 *   <tr><td>Monthly</td><td>{@code 202601}</td><td>{@link PeriodType#MONTH}</td></tr>
 *   <tr><td>Quarterly</td><td>{@code 2026Q1}</td><td>{@link PeriodType#QUARTER}</td></tr>
 *   <tr><td>Half-year (first)</td><td>{@code 2026FH}</td><td>{@link PeriodType#HALF_YEAR}</td></tr>
 *   <tr><td>Half-year (second)</td><td>{@code 2026SH}</td><td>{@link PeriodType#HALF_YEAR}</td></tr>
 *   <tr><td>Annual</td><td>{@code 2026}</td><td>{@link PeriodType#YEAR}</td></tr>
 *   <tr><td>Per-occurrence</td><td>{@code 202601N1}</td><td>{@link PeriodType#ONCE}</td></tr>
 *   <tr><td>Correction</td><td>{@code 202601G1}</td><td>base type + correction sequence</td></tr>
 * </table>
 *
 * <h3>Navigation</h3>
 * <p>Use {@link #previous()}, {@link #next()}, and {@link #samePeriodLastYear()}
 * to navigate between periods of the same type.</p>
 *
 * @param value              the original period string (e.g. {@code "202601"}, {@code "2026Q1"})
 * @param type               the period type
 * @param year               the calendar year
 * @param quarter            the quarter number (1–4), or {@code null} if not a quarterly period
 * @param month              the month number (1–12), or {@code null} if not applicable
 * @param correctionSequence the correction sequence number (for correction periods like {@code 202601G2}),
 *                           or {@code null} if not a correction period
 * @see PeriodType
 * @see #parse(String)
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public record TaxPeriod(
        String value,
        PeriodType type,
        int year,
        Integer quarter,
        Integer month,
        Integer correctionSequence
) {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Compact constructor that validates the period value.
     *
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws TaxDateException     if {@code value} is blank
     */
    public TaxPeriod {
        Objects.requireNonNull(value, "Period value must not be null");
        if (value.isBlank()) {
            throw new TaxDateException("Period value must not be blank");
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a monthly tax period.
     *
     * @param year  the calendar year
     * @param month the month (1–12)
     * @return a {@code TaxPeriod} with value like {@code "202601"}
     * @throws IllegalArgumentException if {@code month} is not between 1 and 12
     */
    public static TaxPeriod ofMonth(int year, int month) {
        if (month < PeriodConstants.JANUARY || month > PeriodConstants.MONTHS_PER_YEAR) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        String val = year + String.format("%02d", month);
        return new TaxPeriod(val, PeriodType.MONTH, year, null, month, null);
    }

    /**
     * Creates a monthly period from a {@link YearMonth}.
     *
     * @param yearMonth the year-month, never {@code null}
     * @return the monthly tax period
     */
    public static TaxPeriod ofMonth(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth, "YearMonth must not be null");
        return ofMonth(yearMonth.getYear(), yearMonth.getMonthValue());
    }

    /**
     * Creates a quarterly tax period.
     *
     * @param year    the calendar year
     * @param quarter the quarter (1–4)
     * @return a {@code TaxPeriod} with value like {@code "2026Q1"}
     * @throws IllegalArgumentException if {@code quarter} is not between 1 and 4
     */
    public static TaxPeriod ofQuarter(int year, int quarter) {
        if (quarter < PeriodConstants.FIRST_QUARTER || quarter > PeriodConstants.QUARTERS_PER_YEAR) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }
        String val = year + "Q" + quarter;
        return new TaxPeriod(val, PeriodType.QUARTER, year, quarter, null, null);
    }

    /**
     * Creates a half-yearly tax period.
     *
     * @param year      the calendar year
     * @param firstHalf {@code true} for the first half (FH), {@code false} for the second half (SH)
     * @return a {@code TaxPeriod} with value like {@code "2026FH"} or {@code "2026SH"}
     */
    public static TaxPeriod ofHalfYear(int year, boolean firstHalf) {
        String val = year + (firstHalf ? "FH" : "SH");
        return new TaxPeriod(val, PeriodType.HALF_YEAR, year, null, null, null);
    }

    /**
     * Creates an annual tax period.
     *
     * @param year the calendar year
     * @return a {@code TaxPeriod} with value like {@code "2026"}
     */
    public static TaxPeriod ofYear(int year) {
        return new TaxPeriod(String.valueOf(year), PeriodType.YEAR, year, null, null, null);
    }

    /**
     * Creates a per-occurrence tax period.
     *
     * @param year  the calendar year
     * @param month the month (1–12)
     * @return a {@code TaxPeriod} with value like {@code "202601N1"}
     * @throws IllegalArgumentException if {@code month} is not between 1 and 12
     */
    public static TaxPeriod ofOnce(int year, int month) {
        if (month < PeriodConstants.JANUARY || month > PeriodConstants.MONTHS_PER_YEAR) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        String val = year + String.format("%02d", month) + "N1";
        return new TaxPeriod(val, PeriodType.ONCE, year, null, month, null);
    }

    // ==================== Parsing ====================

    /**
     * Parses a period string into a {@code TaxPeriod}.
     *
     * <p>The parser recognizes the following formats (checked in order):</p>
     * <ol>
     *   <li>Correction: contains {@code "G"} (e.g. {@code "202601G1"})</li>
     *   <li>Quarterly: contains {@code "Q"} (e.g. {@code "2026Q1"})</li>
     *   <li>Half-year: contains {@code "H"} (e.g. {@code "2026FH"}, {@code "2026SH"})</li>
     *   <li>Per-occurrence: contains {@code "N"} (e.g. {@code "202601N1"})</li>
     *   <li>Monthly: exactly 6 digits (e.g. {@code "202601"})</li>
     *   <li>Annual: exactly 4 digits (e.g. {@code "2026"})</li>
     * </ol>
     *
     * @param value the period string to parse
     * @return the parsed {@code TaxPeriod}
     * @throws NullPointerException  if {@code value} is {@code null}
     * @throws TaxDateException      if the format is not recognized or the value is blank
     * @throws NumberFormatException if numeric parts cannot be parsed
     */
    public static TaxPeriod parse(String value) {
        Objects.requireNonNull(value, "Period value must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new TaxDateException("Period value must not be blank");
        }

        // Dispatch based on the distinguishing character in the format
        if (trimmed.contains("G")) {
            return parseCorrectionPeriod(trimmed);
        }
        if (trimmed.contains("Q")) {
            return parseQuarterPeriod(trimmed);
        }
        if (trimmed.contains("H")) {
            return parseHalfYearPeriod(trimmed);
        }
        if (trimmed.contains("N")) {
            return parseOncePeriod(trimmed);
        }
        if (trimmed.length() == PeriodConstants.MONTH_FORMAT_LENGTH && trimmed.chars().allMatch(Character::isDigit)) {
            return parseMonthPeriod(trimmed);
        }
        if (trimmed.length() == PeriodConstants.YEAR_FORMAT_LENGTH && trimmed.chars().allMatch(Character::isDigit)) {
            return parseYearPeriod(trimmed);
        }

        throw new TaxDateException("Unrecognized period format: " + value);
    }

    /**
     * Parses a correction period like {@code "202601G2"}.
     * The base period is parsed recursively, then the correction sequence is extracted.
     * If no sequence number follows 'G', defaults to 1.
     */
    private static TaxPeriod parseCorrectionPeriod(String value) {
        int gIndex = value.lastIndexOf('G');
        if (gIndex == 0) {
            throw new TaxDateException("Correction period must have a base period before 'G': " + value);
        }
        String base = value.substring(0, gIndex);
        String seqStr = value.substring(gIndex + 1);
        // Default correction sequence is 1 if no number follows 'G'
        int seq;
        if (seqStr.isEmpty()) {
            seq = 1;
        } else {
            try {
                seq = Integer.parseInt(seqStr);
            } catch (NumberFormatException e) {
                throw new TaxDateException("Invalid correction sequence in: " + value, e);
            }
            if (seq < 1) {
                throw new TaxDateException("Correction sequence must be positive, got: " + seq);
            }
        }

        TaxPeriod basePeriod = parse(base);
        return new TaxPeriod(value, basePeriod.type(), basePeriod.year(),
                basePeriod.quarter(), basePeriod.month(), seq);
    }

    /** Parses a quarterly period like {@code "2026Q1"}. */
    private static TaxPeriod parseQuarterPeriod(String value) {
        if (value.length() < PeriodConstants.QUARTER_MIN_LENGTH) {
            throw new TaxDateException("Quarter period must be at least 6 characters: " + value);
        }
        int year;
        int q;
        try {
            year = Integer.parseInt(value.substring(0, PeriodConstants.YEAR_FORMAT_LENGTH));
            q = Integer.parseInt(value.substring(5));
        } catch (NumberFormatException e) {
            throw new TaxDateException("Invalid quarter period format: " + value, e);
        }
        if (q < PeriodConstants.FIRST_QUARTER || q > PeriodConstants.QUARTERS_PER_YEAR) {
            throw new TaxDateException("Quarter must be between 1 and 4, got: " + q);
        }
        return new TaxPeriod(value, PeriodType.QUARTER, year, q, null, null);
    }

    /** Parses a half-year period like {@code "2026FH"} or {@code "2026SH"}. */
    private static TaxPeriod parseHalfYearPeriod(String value) {
        String suffix = value.substring(PeriodConstants.YEAR_FORMAT_LENGTH);
        if (!"FH".equals(suffix) && !"SH".equals(suffix)) {
            throw new TaxDateException("Half-year period must end with 'FH' or 'SH', got: " + value);
        }
        int year;
        try {
            year = Integer.parseInt(value.substring(0, PeriodConstants.YEAR_FORMAT_LENGTH));
        } catch (NumberFormatException e) {
            throw new TaxDateException("Invalid half-year period format: " + value, e);
        }
        return new TaxPeriod(value, PeriodType.HALF_YEAR, year, null, null, null);
    }

    /** Parses a per-occurrence period like {@code "202601N1"}. */
    private static TaxPeriod parseOncePeriod(String value) {
        if (value.length() < PeriodConstants.ONCE_MIN_LENGTH) {
            throw new TaxDateException("Per-occurrence period must be at least 7 characters: " + value);
        }
        int year;
        int month;
        try {
            year = Integer.parseInt(value.substring(0, PeriodConstants.YEAR_FORMAT_LENGTH));
            month = Integer.parseInt(value.substring(PeriodConstants.YEAR_FORMAT_LENGTH, PeriodConstants.MONTH_FORMAT_LENGTH));
        } catch (NumberFormatException e) {
            throw new TaxDateException("Invalid per-occurrence period format: " + value, e);
        }
        if (month < PeriodConstants.JANUARY || month > PeriodConstants.MONTHS_PER_YEAR) {
            throw new TaxDateException("Month must be between 1 and 12, got: " + month);
        }
        return new TaxPeriod(value, PeriodType.ONCE, year, null, month, null);
    }

    /** Parses a monthly period like {@code "202601"}. */
    private static TaxPeriod parseMonthPeriod(String value) {
        int year;
        int month;
        try {
            year = Integer.parseInt(value.substring(0, PeriodConstants.YEAR_FORMAT_LENGTH));
            month = Integer.parseInt(value.substring(PeriodConstants.YEAR_FORMAT_LENGTH, PeriodConstants.MONTH_FORMAT_LENGTH));
        } catch (NumberFormatException e) {
            throw new TaxDateException("Invalid monthly period format: " + value, e);
        }
        if (month < PeriodConstants.JANUARY || month > PeriodConstants.MONTHS_PER_YEAR) {
            throw new TaxDateException("Month must be between 1 and 12, got: " + month);
        }
        return new TaxPeriod(value, PeriodType.MONTH, year, null, month, null);
    }

    /** Parses an annual period like {@code "2026"}. */
    private static TaxPeriod parseYearPeriod(String value) {
        int year;
        try {
            year = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new TaxDateException("Invalid annual period format: " + value, e);
        }
        return new TaxPeriod(value, PeriodType.YEAR, year, null, null, null);
    }

    // ==================== Query Methods ====================

    /**
     * Returns whether this is a correction period (contains a {@code G} suffix).
     *
     * @return {@code true} if {@link #correctionSequence()} is not {@code null}
     */
    public boolean isCorrection() {
        return correctionSequence != null;
    }

    /**
     * Returns whether this is a first-half period (value contains {@code "FH"}).
     *
     * @return {@code true} if this is a first-half ({@code FH}) period
     */
    public boolean isFirstHalf() {
        return value.contains("FH");
    }

    /**
     * Returns whether this is a second-half period (value contains {@code "SH"}).
     *
     * @return {@code true} if this is a second-half ({@code SH}) period
     */
    public boolean isSecondHalf() {
        return value.contains("SH");
    }

    // ==================== Conversion Methods ====================

    /**
     * Converts this period to a {@link YearMonth}.
     *
     * <p>Only periods that have a {@linkplain #month() month} component can be
     * converted (MONTH and ONCE types).</p>
     *
     * @return the corresponding {@code YearMonth}
     * @throws TaxDateException if this period has no month component (e.g. QUARTER, HALF_YEAR, YEAR)
     */
    public YearMonth toYearMonth() {
        if (month == null) {
            throw new TaxDateException("Cannot convert to YearMonth, month is not defined for: " + value);
        }
        return YearMonth.of(year, month);
    }

    /**
     * Converts this period to a {@link DateRange} covering the entire period.
     *
     * <table>
     *   <caption>Date range by period type</caption>
     *   <tr><th>Type</th><th>Start</th><th>End</th></tr>
     *   <tr><td>MONTH</td><td>1st of the month</td><td>last day of the month</td></tr>
     *   <tr><td>QUARTER</td><td>1st of the quarter's first month</td><td>last day of the quarter's last month</td></tr>
     *   <tr><td>HALF_YEAR (FH)</td><td>Jan 1</td><td>Jun 30</td></tr>
     *   <tr><td>HALF_YEAR (SH)</td><td>Jul 1</td><td>Dec 31</td></tr>
     *   <tr><td>YEAR</td><td>Jan 1</td><td>Dec 31</td></tr>
     * </table>
     *
     * @return the date range for this period
     * @throws TaxDateException if this is an ONCE-type period (use {@link #toDateRange(LocalDate)} instead)
     * @see #toDateRange(LocalDate)
     */
    public DateRange toDateRange() {
        return switch (type) {
            case YEAR -> new DateRange(
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31));
            case MONTH -> {
                YearMonth ym = YearMonth.of(year, month);
                yield new DateRange(
                        ym.atDay(1),
                        ym.atEndOfMonth());
            }
            case QUARTER -> {
                // Quarter start month: Q1=1, Q2=4, Q3=7, Q4=10
                int startMonth = (quarter - 1) * 3 + 1;
                int endMonth = quarter * 3;
                yield new DateRange(
                        LocalDate.of(year, startMonth, 1),
                        LocalDate.of(year, endMonth, 1).with(TemporalAdjusters.lastDayOfMonth()));
            }
            case HALF_YEAR -> {
                if (isFirstHalf()) {
                    yield new DateRange(
                            LocalDate.of(year, 1, 1),
                            LocalDate.of(year, 6, 30));
                } else {
                    yield new DateRange(
                            LocalDate.of(year, 7, 1),
                            LocalDate.of(year, 12, 31));
                }
            }
            case ONCE -> throw new TaxDateException(
                    "Cannot convert ONCE-type period to DateRange without a specific date. " +
                    "Use toDateRange(LocalDate onceDate) instead.");
        };
    }

    /**
     * Converts this period to a {@link DateRange}, using the given date for ONCE-type periods.
     *
     * <p>For all period types except ONCE, this behaves identically to {@link #toDateRange()}.
     * For ONCE periods, the provided {@code onceDate} is used as both the start and end
     * of the single-day range.</p>
     *
     * @param onceDate the date to use for ONCE-type periods, never {@code null}
     * @return the date range for this period
     * @throws NullPointerException if {@code onceDate} is {@code null}
     */
    public DateRange toDateRange(LocalDate onceDate) {
        Objects.requireNonNull(onceDate, "onceDate must not be null");
        if (type != PeriodType.ONCE) {
            return toDateRange();
        }
        return new DateRange(onceDate, onceDate);
    }

    // ==================== Navigation Methods ====================

    /**
     * Returns the previous period of the same type.
     *
     * <ul>
     *   <li>MONTH: previous month (e.g. {@code 202601} → {@code 202512})</li>
     *   <li>QUARTER: previous quarter (e.g. {@code 2026Q1} → {@code 2025Q4})</li>
     *   <li>HALF_YEAR: FH → SH of previous year, SH → FH of same year</li>
     *   <li>YEAR: previous year</li>
     *   <li>ONCE: returns {@code this} (per-occurrence has no ordering)</li>
     * </ul>
     *
     * @return the previous period
     */
    public TaxPeriod previous() {
        return switch (type) {
            case MONTH -> {
                YearMonth ym = YearMonth.of(year, month).minusMonths(1);
                yield TaxPeriod.ofMonth(ym.getYear(), ym.getMonthValue());
            }
            case QUARTER -> {
                if (quarter == 1) {
                    yield TaxPeriod.ofQuarter(year - 1, 4);
                }
                yield TaxPeriod.ofQuarter(year, quarter - 1);
            }
            case HALF_YEAR -> isFirstHalf()
                    ? TaxPeriod.ofHalfYear(year - 1, false)
                    : TaxPeriod.ofHalfYear(year, true);
            case YEAR -> TaxPeriod.ofYear(year - 1);
            case ONCE -> this;
        };
    }

    /**
     * Returns the next period of the same type.
     *
     * <ul>
     *   <li>MONTH: next month (e.g. {@code 202612} → {@code 202701})</li>
     *   <li>QUARTER: next quarter (e.g. {@code 2026Q4} → {@code 2027Q1})</li>
     *   <li>HALF_YEAR: FH → SH of same year, SH → FH of next year</li>
     *   <li>YEAR: next year</li>
     *   <li>ONCE: returns {@code this} (per-occurrence has no ordering)</li>
     * </ul>
     *
     * @return the next period
     */
    public TaxPeriod next() {
        return switch (type) {
            case MONTH -> {
                YearMonth ym = YearMonth.of(year, month).plusMonths(1);
                yield TaxPeriod.ofMonth(ym.getYear(), ym.getMonthValue());
            }
            case QUARTER -> {
                if (quarter == 4) {
                    yield TaxPeriod.ofQuarter(year + 1, 1);
                }
                yield TaxPeriod.ofQuarter(year, quarter + 1);
            }
            case HALF_YEAR -> isFirstHalf()
                    ? TaxPeriod.ofHalfYear(year, false)
                    : TaxPeriod.ofHalfYear(year + 1, true);
            case YEAR -> TaxPeriod.ofYear(year + 1);
            case ONCE -> this;
        };
    }

    /**
     * Returns the same period in the previous year.
     *
     * <p>For example, {@code 2026Q1.samePeriodLastYear()} returns {@code 2025Q1}.</p>
     *
     * @return the same period in the previous year
     */
    public TaxPeriod samePeriodLastYear() {
        return switch (type) {
            case MONTH -> TaxPeriod.ofMonth(year - 1, month);
            case QUARTER -> TaxPeriod.ofQuarter(year - 1, quarter);
            case HALF_YEAR -> TaxPeriod.ofHalfYear(year - 1, isFirstHalf());
            case YEAR -> TaxPeriod.ofYear(year - 1);
            case ONCE -> TaxPeriod.ofOnce(year - 1, month);
        };
    }
}
