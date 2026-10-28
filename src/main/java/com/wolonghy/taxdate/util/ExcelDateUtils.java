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

import com.wolonghy.taxdate.constant.PeriodConstants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility methods for handling Excel date cells.
 *
 * <p>Excel stores dates as numeric values (days since the Excel epoch).
 * When reading such cells via Apache POI or similar libraries, the cell
 * value is a {@code double}. This class converts those numeric values
 * to Java date types and formatted strings.</p>
 *
 * <h3>Excel date epoch</h3>
 * <p>Excel's date system starts at January 1, 1900 (day 1). Due to a
 * historical Lotus 1-2-3 bug, Excel incorrectly treats 1900 as a leap year.
 * The conversion here uses the standard algorithm: the numeric value
 * represents the number of days since December 30, 1899.</p>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class ExcelDateUtils {

    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 30);

    private ExcelDateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts an Excel numeric date value to a {@link LocalDate}.
     *
     * @param numericValue the Excel cell numeric value
     * @return the corresponding {@code LocalDate}
     * @throws IllegalArgumentException if the value is NaN
     */
    public static LocalDate toLocalDate(double numericValue) {
        if (Double.isNaN(numericValue)) {
            throw new IllegalArgumentException("Excel date value must not be NaN");
        }
        return EXCEL_EPOCH.plusDays((long) numericValue);
    }

    /**
     * Converts an Excel numeric date value (given as a string) to a {@link LocalDate}.
     *
     * @param cellValue the cell value string
     * @return the corresponding {@code LocalDate}
     * @throws NullPointerException if {@code cellValue} is {@code null}
     * @throws IllegalArgumentException if the input is blank or unparseable
     */
    public static LocalDate toLocalDate(String cellValue) {
        Objects.requireNonNull(cellValue, "Cell value must not be null");
        if (cellValue.isBlank()) {
            throw new IllegalArgumentException("Cell value must not be blank");
        }
        try {
            return toLocalDate(Double.parseDouble(cellValue));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse cell value as number: " + cellValue, e);
        }
    }

    /**
     * Converts an Excel numeric date value to a formatted date string.
     *
     * @param numericValue the Excel cell numeric value
     * @param pattern      the output format pattern (e.g. {@code "yyyy-MM-dd"})
     * @return the formatted date string
     * @throws IllegalArgumentException if the value is NaN
     * @throws NullPointerException if {@code pattern} is {@code null}
     */
    public static String format(double numericValue, String pattern) {
        LocalDate date = toLocalDate(numericValue);
        Objects.requireNonNull(pattern, "Pattern must not be null");
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Converts an Excel numeric date value to a {@code "yyyy-MM-dd"} string.
     *
     * @param numericValue the Excel cell numeric value
     * @return the formatted date string
     * @throws IllegalArgumentException if the value is NaN
     */
    public static String formatDate(double numericValue) {
        return format(numericValue, TaxDateUtils.PATTERN_DATE);
    }

    /**
     * Converts an Excel numeric date value to a {@code "yyyy/MM/dd"} string.
     *
     * @param numericValue the Excel cell numeric value
     * @return the formatted date string
     * @throws IllegalArgumentException if the value is NaN
     */
    public static String formatDateSlash(double numericValue) {
        return format(numericValue, TaxDateUtils.PATTERN_DATE_SLASH);
    }

    /**
     * Checks whether a numeric value is a plausible Excel date.
     *
     * <p>Excel dates are positive numbers within a reasonable range
     * (1 to ~2958465, corresponding to 1900-01-01 to 9999-12-31).</p>
     *
     * @param numericValue the value to check
     * @return {@code true} if the value is a plausible Excel date number
     */
    public static boolean isExcelDate(double numericValue) {
        return !Double.isNaN(numericValue) && numericValue >= 1 && numericValue <= PeriodConstants.EXCEL_DATE_MAX;
    }

    /**
     * Converts a {@link LocalDate} to the corresponding Excel numeric date value.
     *
     * @param date the date to convert
     * @return the Excel numeric value (days since 1899-12-30)
     */
    public static long toExcelNumeric(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return ChronoUnit.DAYS.between(EXCEL_EPOCH, date);
    }
}
