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

package com.wolonghy.taxdate.constant;

/**
 * Numeric constants for period calculations and format validation.
 *
 * <p>This class centralizes magic numbers used throughout the library for
 * period arithmetic, date calculations, and string format validation.</p>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class PeriodConstants {

    private PeriodConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Period Structure ====================

    /** Number of months in a quarter. */
    public static final int MONTHS_PER_QUARTER = 3;

    /** Number of quarters in a year. */
    public static final int QUARTERS_PER_YEAR = 4;

    /** Number of months in a year. */
    public static final int MONTHS_PER_YEAR = 12;

    /** Number of months in a half-year period. */
    public static final int MONTHS_PER_HALF_YEAR = 6;

    // ==================== Key Months ====================

    /** January — first month of the year, declaration month for year-end periods. */
    public static final int JANUARY = 1;

    /** First quarter number. */
    public static final int FIRST_QUARTER = 1;

    /** June — last month of the first half-year. */
    public static final int JUNE = 6;

    /** July — first month of the second half-year, declaration month for first-half periods. */
    public static final int JULY = 7;

    /** December — last month of the year, declaration month for December periods. */
    public static final int DECEMBER = 12;

    // ==================== Period Format Lengths ====================

    /** Character length of annual period format (e.g. "2026"). */
    public static final int YEAR_FORMAT_LENGTH = 4;

    /** Character length of monthly period format (e.g. "202601"). */
    public static final int MONTH_FORMAT_LENGTH = 6;

    /** Minimum character length of quarterly period format (e.g. "2026Q1"). */
    public static final int QUARTER_MIN_LENGTH = 6;

    /** Minimum character length of per-occurrence period format (e.g. "202601N1"). */
    public static final int ONCE_MIN_LENGTH = 7;

    // ==================== Deadline Constants ====================

    /** Standard monthly filing deadline day (15th of the declaration month). */
    public static final int STANDARD_DEADLINE_DAY = 15;

    /** Filing deadline day for past annual periods (31st, typically May 31). */
    public static final int ANNUAL_PAST_DEADLINE_DAY = 31;

    /** Maximum valid day of month. */
    public static final int MAX_DAY_OF_MONTH = 31;

    // ==================== Weekend Adjustment ====================

    /** Days to advance from Saturday to Monday. */
    public static final int SATURDAY_TO_MONDAY = 2;

    /** Days to advance from Sunday to Monday. */
    public static final int SUNDAY_TO_MONDAY = 1;

    /** Maximum iterations for holiday adjustment loops (safety limit). */
    public static final int MAX_HOLIDAY_ITERATIONS = 30;

    // ==================== Excel Date Constants ====================

    /** Maximum valid Excel serial date number (corresponds to 9999-12-31). */
    public static final long EXCEL_DATE_MAX = 2958465L;

    // ==================== Tolerance Constants ====================

    /** Approximate days per month, used for tolerance calculations. */
    public static final double DAYS_PER_MONTH_APPROX = 28.0;
}
