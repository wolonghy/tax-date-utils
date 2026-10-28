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
import com.wolonghy.taxdate.exception.TaxDateException;
import com.wolonghy.taxdate.model.DateRange;
import com.wolonghy.taxdate.model.TaxPeriod;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides arithmetic operations on {@link TaxPeriod} instances:
 * offsets, enumeration, and distance calculations.
 *
 * <p>This class complements {@link TaxPeriodResolver} (which handles flag-based
 * resolution) with direct period arithmetic. For example, you can offset a
 * monthly period by N months, enumerate all quarters in a range, or compute
 * the number of months between two periods.</p>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @see TaxPeriodResolver
 * @see TaxPeriod
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class TaxPeriodCalculator {

    private TaxPeriodCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Offsets a monthly period by the given number of months.
     *
     * <p>Example: offsetting {@code 202601} by {@code 3} returns {@code 202604}.
     * Offsetting by {@code -1} returns {@code 202512}.</p>
     *
     * @param period the base monthly period
     * @param months the number of months to offset (may be negative)
     * @return the offset monthly period
     * @throws NullPointerException if {@code period} is {@code null}
     * @throws TaxDateException if the period is not of type {@link PeriodType#MONTH}
     */
    public static TaxPeriod offsetMonths(TaxPeriod period, int months) {
        Objects.requireNonNull(period, "Period must not be null");
        if (period.type() != PeriodType.MONTH) {
            throw new TaxDateException("Month offset is only supported for monthly periods, got: " + period.type());
        }
        YearMonth ym = YearMonth.of(period.year(), period.month()).plusMonths(months);
        return TaxPeriod.ofMonth(ym.getYear(), ym.getMonthValue());
    }

    /**
     * Offsets a quarterly period by the given number of quarters.
     *
     * <p>Example: offsetting {@code 2026Q1} by {@code 2} returns {@code 2026Q3}.
     * Offsetting {@code 2026Q4} by {@code 1} returns {@code 2027Q1}.
     * Offsetting {@code 2026Q1} by {@code -1} returns {@code 2025Q4}.</p>
     *
     * @param period   the base quarterly period
     * @param quarters the number of quarters to offset (may be negative)
     * @return the offset quarterly period
     * @throws NullPointerException if {@code period} is {@code null}
     * @throws TaxDateException if the period is not of type {@link PeriodType#QUARTER}
     */
    public static TaxPeriod offsetQuarters(TaxPeriod period, int quarters) {
        Objects.requireNonNull(period, "Period must not be null");
        if (period.type() != PeriodType.QUARTER) {
            throw new TaxDateException("Quarter offset is only supported for quarterly periods, got: " + period.type());
        }
        // Convert to an absolute quarter index (year * 4 + quarter - 1), apply the offset,
        // then convert back to (year, quarter). We use Math.floorDiv/floorMod instead of
        // regular division because standard Java division rounds toward zero, which produces
        // incorrect results for negative offsets (e.g. -1 / 4 == 0, but floorDiv(-1, 4) == -1).
        int totalQuarters = (period.year() * 4 + period.quarter() - 1) + quarters;
        int newYear = Math.floorDiv(totalQuarters, 4);
        int newQuarter = Math.floorMod(totalQuarters, 4) + 1;
        return TaxPeriod.ofQuarter(newYear, newQuarter);
    }

    /**
     * Offsets any period type by the given number of years.
     *
     * <p>The period type is preserved; only the year component changes.</p>
     *
     * @param period the base period
     * @param years  the number of years to offset (may be negative)
     * @return the offset period
     * @throws NullPointerException if {@code period} is {@code null}
     */
    public static TaxPeriod offsetYears(TaxPeriod period, int years) {
        Objects.requireNonNull(period, "Period must not be null");
        return switch (period.type()) {
            case MONTH -> TaxPeriod.ofMonth(period.year() + years, period.month());
            case QUARTER -> TaxPeriod.ofQuarter(period.year() + years, period.quarter());
            case HALF_YEAR -> TaxPeriod.ofHalfYear(period.year() + years, period.isFirstHalf());
            case YEAR -> TaxPeriod.ofYear(period.year() + years);
            case ONCE -> TaxPeriod.ofOnce(period.year() + years, period.month());
        };
    }

    /**
     * Enumerates all monthly periods from {@code from} to {@code to} (inclusive).
     *
     * <p>Both periods are converted to their date ranges, and every calendar month
     * that overlaps with the combined range is returned.</p>
     *
     * @param from the start period
     * @param to   the end period
     * @return a list of monthly {@code TaxPeriod} instances in chronological order
     * @throws NullPointerException if either argument is {@code null}
     */
    public static List<TaxPeriod> enumerateMonths(TaxPeriod from, TaxPeriod to) {
        Objects.requireNonNull(from, "From period must not be null");
        Objects.requireNonNull(to, "To period must not be null");

        LocalDate startDate = from.toDateRange().start();
        LocalDate endDate = to.toDateRange().end();

        List<TaxPeriod> periods = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth endYm = YearMonth.from(endDate);

        while (!current.isAfter(endYm)) {
            periods.add(TaxPeriod.ofMonth(current.getYear(), current.getMonthValue()));
            current = current.plusMonths(1);
        }
        return periods;
    }

    /**
     * Enumerates all quarterly periods from {@code from} to {@code to} (inclusive).
     *
     * @param from the start period (must be a quarter or convertible to one)
     * @param to   the end period (must be a quarter or convertible to one)
     * @return a list of quarterly {@code TaxPeriod} instances in chronological order
     * @throws NullPointerException if either argument is {@code null}
     */
    public static List<TaxPeriod> enumerateQuarters(TaxPeriod from, TaxPeriod to) {
        Objects.requireNonNull(from, "From period must not be null");
        Objects.requireNonNull(to, "To period must not be null");

        List<TaxPeriod> periods = new ArrayList<>();
        int startYear = from.year();
        int startQ = from.quarter() != null ? from.quarter() : 1;
        int endYear = to.year();
        int endQ = to.quarter() != null ? to.quarter() : 4;

        // Convert to absolute quarter index and iterate
        int current = startYear * 4 + startQ - 1;
        int end = endYear * 4 + endQ - 1;

        while (current <= end) {
            int y = current / 4;
            int q = current % 4 + 1;
            periods.add(TaxPeriod.ofQuarter(y, q));
            current++;
        }
        return periods;
    }

    /**
     * Returns the number of months between two periods, measured from their start dates.
     *
     * @param from the start period
     * @param to   the end period
     * @return the number of months between the two periods (may be negative if {@code to} is before {@code from})
     * @throws NullPointerException if either argument is {@code null}
     */
    public static long monthsBetween(TaxPeriod from, TaxPeriod to) {
        Objects.requireNonNull(from, "From period must not be null");
        Objects.requireNonNull(to, "To period must not be null");
        DateRange fromRange = from.toDateRange();
        DateRange toRange = to.toDateRange();
        return ChronoUnit.MONTHS.between(fromRange.start(), toRange.start());
    }

    /**
     * Returns the number of quarters between two quarterly periods.
     *
     * @param from the start quarter
     * @param to   the end quarter
     * @return the number of quarters between the two periods
     * @throws NullPointerException if either argument is {@code null}
     */
    public static long quartersBetween(TaxPeriod from, TaxPeriod to) {
        Objects.requireNonNull(from, "From period must not be null");
        Objects.requireNonNull(to, "To period must not be null");
        int fromTotal = from.year() * 4 + (from.quarter() != null ? from.quarter() : 1) - 1;
        int toTotal = to.year() * 4 + (to.quarter() != null ? to.quarter() : 1) - 1;
        return toTotal - fromTotal;
    }
}
