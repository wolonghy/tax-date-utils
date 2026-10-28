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

package com.wolonghy.taxdate.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Converts between {@link LocalDate} and the OA Date serial format.
 *
 * <p>OA (OLE Automation) Date is a legacy date format used by some enterprise
 * systems (e.g. Kingdee, UFIDA). It represents a date as the number of days
 * since the epoch {@code 1899-12-30}, wrapped in the format
 * {@code /OADate(n)/}.</p>
 *
 * <h3>Examples</h3>
 * <ul>
 *   <li>{@code toOaDate("2026/01/15")} → {@code "/OADate(46037)/"}</li>
 *   <li>{@code fromOaDate("/OADate(46037)/")} → {@code "2026-01-15"}</li>
 * </ul>
 *
 * <p>All methods throw {@link IllegalArgumentException} for invalid input or
 * {@link NullPointerException} for {@code null} input, making error conditions
 * explicit rather than silently returning {@code null}.</p>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class OaDateConverter {

    /** OA Date epoch: December 30, 1899. */
    private static final LocalDate EPOCH = LocalDate.of(1899, 12, 30);
    private static final DateTimeFormatter SLASH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DASH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String OA_DATE_PREFIX = "/OADate(";
    private static final String OA_DATE_SUFFIX = ")/";

    private OaDateConverter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a date string in {@code "yyyy/MM/dd"} format to OA Date format.
     *
     * @param dateStr the date string (e.g. {@code "2026/01/15"})
     * @return the OA Date string (e.g. {@code "/OADate(46037)/"})
     * @throws NullPointerException if {@code dateStr} is {@code null}
     * @throws IllegalArgumentException if the input is blank or unparseable
     */
    public static String toOaDate(String dateStr) {
        Objects.requireNonNull(dateStr, "Date string must not be null");
        if (dateStr.isBlank()) {
            throw new IllegalArgumentException("Date string must not be blank");
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, SLASH_FORMATTER);
            long days = ChronoUnit.DAYS.between(EPOCH, date);
            return OA_DATE_PREFIX + days + OA_DATE_SUFFIX;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Failed to parse date: " + dateStr, e);
        }
    }

    /**
     * Converts an OA Date string to a date string in {@code "yyyy-MM-dd"} format.
     *
     * @param oaDate the OA Date string (e.g. {@code "/OADate(46037)/"})
     * @return the date string (e.g. {@code "2026-01-15"})
     * @throws NullPointerException if {@code oaDate} is {@code null}
     * @throws IllegalArgumentException if the input is malformed
     */
    public static String fromOaDate(String oaDate) {
        Objects.requireNonNull(oaDate, "OA Date string must not be null");
        if (!oaDate.contains(OA_DATE_PREFIX)) {
            throw new IllegalArgumentException("Invalid OA Date format: " + oaDate);
        }
        try {
            int startIdx = oaDate.indexOf(OA_DATE_PREFIX) + OA_DATE_PREFIX.length();
            int endIdx = oaDate.indexOf(')', startIdx);
            if (endIdx < 0) {
                throw new IllegalArgumentException("Malformed OA Date string (missing closing paren): " + oaDate);
            }
            long days = Long.parseLong(oaDate.substring(startIdx, endIdx));
            LocalDate date = EPOCH.plusDays(days);
            return date.format(DASH_FORMATTER);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid OA Date numeric value: " + oaDate, e);
        }
    }

    /**
     * Converts a {@link LocalDate} to OA Date format.
     *
     * @param date the date to convert
     * @return the OA Date string (e.g. {@code "/OADate(46037)/"})
     * @throws NullPointerException if the input {@code date} is {@code null}
     */
    public static String toOaDate(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        long days = ChronoUnit.DAYS.between(EPOCH, date);
        return OA_DATE_PREFIX + days + OA_DATE_SUFFIX;
    }

    /**
     * Converts an OA Date string to a {@link LocalDate}.
     *
     * @param oaDate the OA Date string (e.g. {@code "/OADate(46037)/"})
     * @return the corresponding {@code LocalDate}
     * @throws NullPointerException if {@code oaDate} is {@code null}
     * @throws IllegalArgumentException if the input is malformed
     */
    public static LocalDate fromOaDateToDate(String oaDate) {
        Objects.requireNonNull(oaDate, "OA Date string must not be null");
        if (!oaDate.contains(OA_DATE_PREFIX)) {
            throw new IllegalArgumentException("Invalid OA Date format: " + oaDate);
        }
        try {
            int startIdx = oaDate.indexOf(OA_DATE_PREFIX) + OA_DATE_PREFIX.length();
            int endIdx = oaDate.indexOf(')', startIdx);
            if (endIdx < 0) {
                throw new IllegalArgumentException("Malformed OA Date string (missing closing paren): " + oaDate);
            }
            long days = Long.parseLong(oaDate.substring(startIdx, endIdx));
            return EPOCH.plusDays(days);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid OA Date numeric value: " + oaDate, e);
        }
    }
}
