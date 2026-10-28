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

import com.wolonghy.taxdate.constant.PeriodConstants;
import com.wolonghy.taxdate.model.TaxPeriod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Calculates tax filing deadlines with automatic weekend and holiday adjustment.
 *
 * <p>In China's tax system, the default monthly filing deadline is the 15th of
 * the declaration month. If the 15th falls on a weekend or public holiday,
 * the deadline is extended to the next business day.</p>
 *
 * <p>The "declaration month" is the month in which a period must be filed.
 * For example, the January 2026 monthly period ({@code 202601}) is declared
 * in February 2026, so the default deadline is February 15, 2026.</p>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @see TaxPeriod
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class TaxDeadlineCalculator {

    /** The default filing deadline day (15th of the declaration month). */
    private static final int DEFAULT_MONTHLY_DEADLINE_DAY = PeriodConstants.STANDARD_DEADLINE_DAY;

    private TaxDeadlineCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates the filing deadline for a period using the default deadline day (15th).
     *
     * <p>The result is automatically adjusted if it falls on a weekend
     * (Saturday → next Monday, Sunday → next Monday).</p>
     *
     * @param period the tax period to calculate the deadline for
     * @return the adjusted deadline date
     * @throws NullPointerException if {@code period} is {@code null}
     */
    public static LocalDate calculateDeadline(TaxPeriod period) {
        return calculateDeadline(period, DEFAULT_MONTHLY_DEADLINE_DAY);
    }

    /**
     * Calculates the filing deadline for a period using a custom deadline day.
     *
     * <p>If the deadline day exceeds the number of days in the declaration month,
     * the last day of the month is used instead. The result is then adjusted
     * for weekends.</p>
     *
     * @param period     the tax period
     * @param deadlineDay the day of the declaration month (1–31)
     * @return the adjusted deadline date
     * @throws NullPointerException     if {@code period} is {@code null}
     * @throws IllegalArgumentException if {@code deadlineDay} is not between 1 and 31
     */
    public static LocalDate calculateDeadline(TaxPeriod period, int deadlineDay) {
        Objects.requireNonNull(period, "Tax period must not be null");
        if (deadlineDay < 1 || deadlineDay > 31) {
            throw new IllegalArgumentException("Deadline day must be between 1 and 31");
        }

        LocalDate declarationStart = getDeclarationStart(period);
        // Clamp the deadline day to the actual number of days in the month
        LocalDate deadline = declarationStart.withDayOfMonth(
                Math.min(deadlineDay, declarationStart.lengthOfMonth()));

        return adjustForWeekend(deadline);
    }

    /**
     * Adjusts a date forward if it falls on a weekend.
     *
     * <ul>
     *   <li>Saturday → following Monday (+2 days)</li>
     *   <li>Sunday → following Monday (+1 day)</li>
     *   <li>Weekday → unchanged</li>
     * </ul>
     *
     * @param date the date to adjust, never {@code null}
     * @return the adjusted date (same date if it was already a weekday)
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public static LocalDate adjustForWeekend(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * Adjusts a date forward if it falls on a weekend or a public holiday.
     *
     * <p>First adjusts for weekends, then checks if the resulting date is in
     * the holiday set. If so, advances by one day and re-adjusts for weekends.
     * This process repeats until a valid business day is found.</p>
     *
     * @param date    the date to adjust, never {@code null}
     * @param holidays a set of public holiday dates (may be {@code null})
     * @return the adjusted date
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public static LocalDate adjustForWeekendAndHoliday(LocalDate date,
                                                       Set<LocalDate> holidays) {
        Objects.requireNonNull(date, "Date must not be null");
        LocalDate adjusted = adjustForWeekend(date);
        int maxIterations = PeriodConstants.MAX_HOLIDAY_ITERATIONS;
        int iterations = 0;
        
        while (holidays != null && holidays.contains(adjusted) && iterations++ < maxIterations) {
            // Advance one day past the holiday, then re-check for weekend
            adjusted = adjusted.plusDays(1);
            adjusted = adjustForWeekend(adjusted);
        }
        return adjusted;
    }

    /**
     * Adjusts a date forward considering weekends, public holidays, and makeup workdays.
     *
     * <p>In China's tax system, the government publishes an annual holiday schedule that
     * includes both public holidays and "makeup workdays" (调休工作日) — weekend days
     * that are designated as working days to compensate for extended holiday periods.</p>
     *
     * <p>The adjustment algorithm:</p>
     * <ol>
     *   <li>If the date falls on a holiday → advance to the next day, then re-evaluate</li>
     *   <li>If the date falls on a weekend that is NOT a makeup workday → advance to Monday</li>
     *   <li>If the date falls on a makeup workday (even if it's a weekend) → it's valid, no change</li>
     *   <li>Repeat until a valid business day is found</li>
     * </ol>
     *
     * @param date           the date to adjust, never {@code null}
     * @param holidays       a set of public holiday dates (may be {@code null})
     * @param makeupWorkdays a set of weekend dates that are designated as working days (may be {@code null})
     * @return the adjusted date (the next valid business day)
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public static LocalDate adjustForChineseHoliday(LocalDate date,
                                                     Set<LocalDate> holidays,
                                                     Set<LocalDate> makeupWorkdays) {
        Objects.requireNonNull(date, "Date must not be null");
        LocalDate adjusted = date;
        int maxIterations = PeriodConstants.MAX_HOLIDAY_ITERATIONS;
        int iterations = 0;

        while (iterations++ < maxIterations) {
            boolean isHoliday = holidays != null && holidays.contains(adjusted);
            boolean isMakeupWorkday = makeupWorkdays != null && makeupWorkdays.contains(adjusted);
            boolean isWeekend = adjusted.getDayOfWeek() == DayOfWeek.SATURDAY
                    || adjusted.getDayOfWeek() == DayOfWeek.SUNDAY;

            if (isHoliday) {
                adjusted = adjusted.plusDays(1);
                continue;
            }
            if (isWeekend && !isMakeupWorkday) {
                adjusted = adjusted.plusDays(1);
                continue;
            }
            break;
        }
        return adjusted;
    }

    /**
     * Calculates the filing deadline with Chinese holiday schedule adjustment.
     *
     * <p>Combines the deadline calculation with the full Chinese holiday adjustment
     * algorithm, including makeup workday handling.</p>
     *
     * @param period         the tax period
     * @param holidays       public holiday dates
     * @param makeupWorkdays weekend dates designated as working days
     * @return the adjusted deadline date
     * @throws NullPointerException if {@code period} is {@code null}
     */
    public static LocalDate calculateDeadlineWithChineseHoliday(TaxPeriod period,
                                                                 Set<LocalDate> holidays,
                                                                 Set<LocalDate> makeupWorkdays) {
        return calculateDeadlineWithChineseHoliday(period, DEFAULT_MONTHLY_DEADLINE_DAY, holidays, makeupWorkdays);
    }

    /**
     * Calculates the filing deadline with Chinese holiday schedule adjustment and custom deadline day.
     *
     * @param period         the tax period
     * @param deadlineDay    the filing deadline day (1–31)
     * @param holidays       public holiday dates
     * @param makeupWorkdays weekend dates designated as working days
     * @return the adjusted deadline date
     * @throws NullPointerException     if {@code period} is {@code null}
     * @throws IllegalArgumentException if {@code deadlineDay} is not between 1 and 31
     */
    public static LocalDate calculateDeadlineWithChineseHoliday(TaxPeriod period,
                                                                 int deadlineDay,
                                                                 Set<LocalDate> holidays,
                                                                 Set<LocalDate> makeupWorkdays) {
        Objects.requireNonNull(period, "Tax period must not be null");
        if (deadlineDay < 1 || deadlineDay > 31) {
            throw new IllegalArgumentException("Deadline day must be between 1 and 31");
        }

        LocalDate declarationStart = getDeclarationStart(period);
        LocalDate deadline = declarationStart.withDayOfMonth(
                Math.min(deadlineDay, declarationStart.lengthOfMonth()));

        return adjustForChineseHoliday(deadline, holidays, makeupWorkdays);
    }

    /**
     * Returns the natural (unadjusted) deadline day for a period type.
     *
     * <p>For most period types the natural deadline is the 15th. For past
     * annual periods the deadline is the 31st (of the declaration month).</p>
     *
     * @param period the tax period
     * @return the natural deadline day (15 or 31)
     */
    public static int getNaturalDeadlineDay(TaxPeriod period) {
        return switch (period.type()) {
            case MONTH -> PeriodConstants.STANDARD_DEADLINE_DAY;
            case QUARTER -> PeriodConstants.STANDARD_DEADLINE_DAY;
            case HALF_YEAR -> PeriodConstants.STANDARD_DEADLINE_DAY;
            case YEAR -> {
                // Past annual periods have a later deadline (May 31)
                if (period.year() < LocalDate.now().getYear()) {
                    yield PeriodConstants.ANNUAL_PAST_DEADLINE_DAY;
                }
                yield PeriodConstants.STANDARD_DEADLINE_DAY;
            }
            case ONCE -> PeriodConstants.STANDARD_DEADLINE_DAY;
        };
    }

    // ==================== Private Helpers ====================

    /**
     * Returns the first day of the declaration month for the given period.
     * This is the month in which the period must be filed.
     */
    private static LocalDate getDeclarationStart(TaxPeriod period) {
        return switch (period.type()) {
            case MONTH -> {
                int m = period.month();
                int y = period.year();
                // December's declaration month is January of the next year
                if (m == 12) {
                    yield LocalDate.of(y + 1, 1, 1);
                }
                yield LocalDate.of(y, m + 1, 1);
            }
            case QUARTER -> {
                int q = period.quarter();
                int y = period.year();
                // Q4's declaration month is January of the next year
                if (q == 4) {
                    yield LocalDate.of(y + 1, 1, 1);
                }
                yield LocalDate.of(y, q * 3 + 1, 1);
            }
            case HALF_YEAR -> {
                if (period.isFirstHalf()) {
                    yield LocalDate.of(period.year(), 7, 1);
                }
                yield LocalDate.of(period.year() + 1, 1, 1);
            }
            case YEAR -> LocalDate.of(period.year() + 1, 1, 1);
            case ONCE -> LocalDate.of(period.year(), period.month(), 1);
        };
    }
}
