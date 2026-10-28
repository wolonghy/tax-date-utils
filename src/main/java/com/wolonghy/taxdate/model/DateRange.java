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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * An immutable, inclusive date range defined by a {@linkplain #start() start}
 * and {@linkplain #end() end} {@link LocalDate}.
 *
 * <p>Both boundaries are inclusive — a date {@code d} is "in" the range when
 * {@code start <= d <= end}. The constructor validates that {@code start} is
 * not after {@code end}.</p>
 *
 * <p>Typical usage: convert a {@link TaxPeriod} to a {@code DateRange} via
 * {@link TaxPeriod#toDateRange()}, then query containment or overlap.</p>
 *
 * @param start the first day of the range (inclusive), never {@code null}
 * @param end   the last day of the range (inclusive), never {@code null} and not before {@code start}
 * @see TaxPeriod#toDateRange()
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public record DateRange(LocalDate start, LocalDate end) {

    /**
     * Compact constructor that validates the range invariant.
     *
     * @throws NullPointerException if {@code start} or {@code end} is {@code null}
     * @throws IllegalArgumentException if {@code start} is after {@code end}
     */
    public DateRange {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date: " + start + " > " + end);
        }
    }

    /**
     * Formats the {@linkplain #start() start} date using the given formatter.
     *
     * @param formatter the formatter to apply, never {@code null}
     * @return the formatted start date string
     */
    public String formatStart(DateTimeFormatter formatter) {
        return start.format(formatter);
    }

    /**
     * Formats the {@linkplain #end() end} date using the given formatter.
     *
     * @param formatter the formatter to apply, never {@code null}
     * @return the formatted end date string
     */
    public String formatEnd(DateTimeFormatter formatter) {
        return end.format(formatter);
    }

    /**
     * Returns the total number of days in this range, inclusive of both endpoints.
     *
     * <p>For example, a range from {@code 2026-01-01} to {@code 2026-01-31}
     * returns {@code 31}.</p>
     *
     * @return the day count (always &ge; 1)
     */
    public long daysBetween() {
        // +1 because both start and end are inclusive
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /**
     * Returns the total number of months spanned by this range, inclusive.
     *
     * <p>For example, a range from {@code 2026-01-15} to {@code 2026-03-10}
     * returns {@code 3} (January, February, March).</p>
     *
     * @return the month count (always &ge; 1)
     */
    public long monthsBetween() {
        // Normalize to the 1st of each month so partial months still count
        return ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)) + 1;
    }

    /**
     * Checks whether the given date falls within this range (inclusive).
     *
     * @param date the date to test, never {@code null}
     * @return {@code true} if {@code start <= date <= end}
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Checks whether this range overlaps with another range.
     *
     * <p>Two ranges overlap when they share at least one common date.
     * Adjacent but non-overlapping ranges (e.g. {@code [Jan 1..Jan 31]}
     * and {@code [Feb 1..Feb 28]}) do <em>not</em> overlap.</p>
     *
     * @param other the other range to test, never {@code null}
     * @return {@code true} if the two ranges share at least one date
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "Other date range must not be null");
        return !this.start.isAfter(other.end) && !other.start.isAfter(this.end);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a {@code DateRange} by parsing two date strings with the given pattern.
     *
     * @param startStr the start date string, never {@code null}
     * @param endStr   the end date string, never {@code null}
     * @param pattern  the date format pattern, never {@code null}
     * @return a new {@code DateRange}
     * @throws IllegalArgumentException if the strings cannot be parsed or start is after end
     * @throws NullPointerException     if any argument is {@code null}
     */
    public static DateRange parse(String startStr, String endStr, String pattern) {
        Objects.requireNonNull(startStr, "Start string must not be null");
        Objects.requireNonNull(endStr, "End string must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDate start = LocalDate.parse(startStr, fmt);
        LocalDate end = LocalDate.parse(endStr, fmt);
        return new DateRange(start, end);
    }

    /**
     * Creates a {@code DateRange} by parsing two {@code "yyyy-MM-dd"} date strings.
     *
     * @param startStr the start date string
     * @param endStr   the end date string
     * @return a new {@code DateRange}
     */
    public static DateRange parse(String startStr, String endStr) {
        return parse(startStr, endStr, "yyyy-MM-dd");
    }
}
