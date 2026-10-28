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

import com.wolonghy.taxdate.constant.PeriodConstants;
import com.wolonghy.taxdate.constant.PeriodFlag;
import com.wolonghy.taxdate.constant.PeriodType;
import com.wolonghy.taxdate.constant.QuickPeriod;
import com.wolonghy.taxdate.exception.TaxDateException;
import com.wolonghy.taxdate.model.DeclarationMonth;
import com.wolonghy.taxdate.model.TaxPeriod;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves tax periods by flag codes, detects period types, determines
 * declaration months, and provides quarter/month navigation.
 *
 * <p>This is the main entry point for period resolution logic. Given a base
 * period and a flag code, it computes the target period according to
 * Chinese tax filing conventions.</p>
 *
 * <h3>Flag code semantics</h3>
 * <table>
 *   <caption>Period flag codes</caption>
 *   <tr><th>Flag</th><th>Meaning</th></tr>
 *   <tr><td>{@code "0"}</td><td>Current period (identity)</td></tr>
 *   <tr><td>{@code "1"}</td><td>Previous period</td></tr>
 *   <tr><td>{@code "2"}</td><td>Same period last year</td></tr>
 *   <tr><td>{@code "3"}</td><td>End of last year</td></tr>
 *   <tr><td>{@code "4"}</td><td>Previous period within current year</td></tr>
 *   <tr><td>{@code "yNN"}</td><td>Specific month of the year (e.g. {@code "y03"} → March)</td></tr>
 *   <tr><td>{@code "qN"}</td><td>Specific quarter (e.g. {@code "q2"} → Q2)</td></tr>
 * </table>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @see TaxPeriodCalculator
 * @see TaxPeriod
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class TaxPeriodResolver {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private TaxPeriodResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Current Period ====================

    /**
     * Returns the current month as a period string in {@code "yyyyMM"} format.
     *
     * @return the current period (e.g. {@code "202610"})
     */
    public static String getCurrentPeriod() {
        return LocalDate.now().format(MONTH_FMT);
    }

    /**
     * Resolves a period string from the current date and the given quick period type.
     *
     * <p>This method computes a period string based on the current system date and the
     * specified {@link QuickPeriod} enum constant. Each constant maps to a specific
     * period granularity and calculation rule, as summarized below:</p>
     *
     * <table>
     *   <caption>Quick period to period string mapping</caption>
     *   <tr><th>{@code QuickPeriod}</th><th>Returns</th><th>Example (current date: 2026-10-28)</th></tr>
     *   <tr><td>{@link QuickPeriod#CURRENT_MONTH}</td><td>Current month</td><td>{@code "202610"}</td></tr>
     *   <tr><td>{@link QuickPeriod#PREVIOUS_MONTH}</td><td>Previous month</td><td>{@code "202609"}</td></tr>
     *   <tr><td>{@link QuickPeriod#CURRENT_QUARTER}</td><td>Current quarter</td><td>{@code "2026Q4"}</td></tr>
     *   <tr><td>{@link QuickPeriod#PREVIOUS_YEAR}</td><td>Previous year</td><td>{@code "2025"}</td></tr>
     *   <tr><td>{@link QuickPeriod#FIRST_HALF}</td><td>First half of current year</td><td>{@code "2026FH"}</td></tr>
     *   <tr><td>{@link QuickPeriod#SECOND_HALF}</td><td>Second half of current year</td><td>{@code "2026SH"}</td></tr>
     *   <tr><td>{@link QuickPeriod#CURRENT_MONTH_OCCURRENCE}</td><td>Current month per-occurrence</td><td>{@code "202610N1"}</td></tr>
     * </table>
     *
     * <p>The quarter calculation derives the quarter number from the current month using
     * the formula: {@code quarter = (month - 1) / 3 + 1}, where months 1–3 map to Q1,
     * months 4–6 map to Q2, and so on.</p>
     *
     * @param quickPeriod the quick period type indicating which period to compute,
     *                    never {@code null}
     * @return the period string corresponding to the current date and the given type
     * @throws NullPointerException if {@code quickPeriod} is {@code null}
     */
    public static String resolveQuickPeriod(QuickPeriod quickPeriod) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        return switch (quickPeriod) {
            case CURRENT_MONTH -> now.format(MONTH_FMT);
            case PREVIOUS_MONTH -> now.minusMonths(1).format(MONTH_FMT);
            case CURRENT_QUARTER -> {
                int quarter = (month - 1) / 3 + 1;
                yield year + "Q" + quarter;
            }
            case PREVIOUS_YEAR -> String.valueOf(year - 1);
            case FIRST_HALF -> year + "FH";
            case SECOND_HALF -> year + "SH";
            case CURRENT_MONTH_OCCURRENCE -> now.format(MONTH_FMT) + "N1";
        };
    }

    // ==================== Target Period Resolution ====================

    /**
     * Resolves a target period from a base period and a flag code.
     *
     * <p>The resolution logic dispatches based on the flag format and the
     * period type:</p>
     * <ul>
     *   <li>Flags starting with {@code "y"} resolve to a specific month of the year</li>
     *   <li>Flags starting with {@code "q"} resolve to a specific quarter</li>
     *   <li>Numeric flags are resolved according to the period type (MONTH, QUARTER, etc.)</li>
     *   <li>{@code null} or blank flags return the original period unchanged</li>
     * </ul>
     *
     * <p>Note: some flag resolutions may change the period type. For example,
     * flag {@code "3"} on a quarterly period returns December of the previous
     * year as a monthly period, and flag {@code "9"} on a monthly period
     * appends an occurrence suffix ({@code "N1"}), converting it to a
     * per-occurrence period.</p>
     *
     * @param period the base period string (e.g. {@code "202601"})
     * @param flag   the flag code (e.g. {@code "0"}, {@code "1"}, {@code "y03"})
     * @return the resolved target period string
     * @throws NullPointerException if {@code period} is {@code null}
     * @throws TaxDateException     if {@code period} is blank
     */
    public static String getTargetPeriod(String period, String flag) {
        Objects.requireNonNull(period, "Period must not be null");
        if (period.isBlank()) {
            throw new TaxDateException("Period must not be blank");
        }
        // Null or blank flag means "return as-is"
        if (flag == null || flag.isBlank()) {
            return period;
        }

        // Special prefix-based flags
        if (flag.startsWith("y")) {
            return resolveYearMonthFlag(period, flag);
        }
        if (flag.startsWith("q")) {
            return resolveQuarterFlag(period, flag);
        }

        TaxPeriod taxPeriod = TaxPeriod.parse(period);

        // Correction periods have their own resolution logic
        if (taxPeriod.isCorrection()) {
            return resolveCorrectionPeriod(taxPeriod, flag);
        }

        return switch (taxPeriod.type()) {
            case QUARTER -> resolveQuarterPeriod(taxPeriod, flag);
            case HALF_YEAR -> resolveHalfYearPeriod(taxPeriod, flag);
            case ONCE -> resolveOncePeriod(taxPeriod, flag);
            case MONTH -> resolveMonthPeriod(taxPeriod, flag);
            case YEAR -> resolveYearPeriod(taxPeriod, flag);
        };
    }

    // ==================== Period Type Detection ====================

    /**
     * Detects the {@link PeriodType} of a period string.
     *
     * @param period the period string to analyze
     * @return the detected period type
     */
    public static PeriodType detectPeriodType(String period) {
        return TaxPeriod.parse(period).type();
    }

    /**
     * Determines the {@link PeriodType} for a given period string.
     *
     * <p>The type is inferred from the period format:</p>
     * <ul>
     *   <li>4-digit all-numeric → {@link PeriodType#YEAR}</li>
     *   <li>Contains {@code "Q"} → {@link PeriodType#QUARTER}</li>
     *   <li>Contains {@code "H"} → {@link PeriodType#HALF_YEAR}</li>
     *   <li>Contains {@code "N"} → {@link PeriodType#ONCE}</li>
     *   <li>Otherwise → {@link PeriodType#MONTH}</li>
     * </ul>
     *
     * @param period the period string
     * @return the period type
     * @throws NullPointerException if {@code period} is {@code null}
     * @throws TaxDateException     if {@code period} is blank
     */
    public static PeriodType getDeclarationCycle(String period) {
        Objects.requireNonNull(period, "Period must not be null");
        if (period.isBlank()) {
            throw new TaxDateException("Period must not be blank");
        }

        if (period.length() == 4 && period.chars().allMatch(Character::isDigit)) {
            return PeriodType.YEAR;
        }
        if (period.contains("Q")) {
            return PeriodType.QUARTER;
        }
        if (period.contains("H")) {
            return PeriodType.HALF_YEAR;
        }
        if (period.contains("N")) {
            return PeriodType.ONCE;
        }
        return PeriodType.MONTH;
    }

    // ==================== Declaration Month ====================

    /**
     * Determines the {@link DeclarationMonth} for a given period.
     *
     * <p>The declaration month is the month in which the period must be filed.
     * Generally it is the month after the period ends, with special handling
     * for year-end periods (December, Q4, annual) which are declared in
     * January of the following year.</p>
     *
     * @param period the period string
     * @return the declaration month
     * @throws NullPointerException if {@code period} is {@code null}
     * @throws TaxDateException     if {@code period} is blank
     */
    public static DeclarationMonth getDeclarationMonth(String period) {
        Objects.requireNonNull(period, "Period must not be null");
        if (period.isBlank()) {
            throw new TaxDateException("Period must not be blank");
        }

        TaxPeriod taxPeriod = TaxPeriod.parse(period);

        return switch (taxPeriod.type()) {
            // Annual periods are declared in January of the next year
            case YEAR -> new DeclarationMonth(taxPeriod.year() + 1, 1);
            case QUARTER -> {
                int q = taxPeriod.quarter();
                // Q4 is declared in January of the next year
                if (q == 4) {
                    yield new DeclarationMonth(taxPeriod.year() + 1, 1);
                }
                // Other quarters are declared in the month after the quarter ends
                yield new DeclarationMonth(taxPeriod.year(), q * 3 + 1);
            }
            case HALF_YEAR -> {
                if (taxPeriod.isFirstHalf()) {
                    yield new DeclarationMonth(taxPeriod.year(), 7);
                }
                yield new DeclarationMonth(taxPeriod.year() + 1, 1);
            }
            case ONCE -> new DeclarationMonth(taxPeriod.year(), taxPeriod.month());
            case MONTH -> {
                int m = taxPeriod.month();
                // December is declared in January of the next year
                if (m == 12) {
                    yield new DeclarationMonth(taxPeriod.year() + 1, 1);
                }
                yield new DeclarationMonth(taxPeriod.year(), m + 1);
            }
        };
    }

    /**
     * Determines the {@link PeriodType} from a validity period (date range).
     *
     * <p>The type is inferred from the number of months spanned:</p>
     * <ul>
     *   <li>1 month → {@link PeriodType#MONTH}</li>
     *   <li>3 months → {@link PeriodType#QUARTER}</li>
     *   <li>6 months → {@link PeriodType#HALF_YEAR}</li>
     *   <li>12 months → {@link PeriodType#YEAR}</li>
     *   <li>1 day → {@link PeriodType#ONCE}</li>
     * </ul>
     *
     * @param startDate the start date in {@code "yyyy-MM-dd"} format
     * @param endDate   the end date in {@code "yyyy-MM-dd"} format
     * @return the inferred period type
     * @throws TaxDateException if the type cannot be determined
     */
    public static PeriodType getPeriodTypeByValidity(String startDate, String endDate) {
        Objects.requireNonNull(startDate, "Start date must not be null");
        Objects.requireNonNull(endDate, "End date must not be null");
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return getPeriodTypeByValidity(start, end);
    }

    /**
     * Determines the {@link PeriodType} from a validity period (date range).
     *
     * @param start the start date
     * @param end   the end date
     * @return the inferred period type
     * @throws TaxDateException if the type cannot be determined
     */
    public static PeriodType getPeriodTypeByValidity(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days == 1) return PeriodType.ONCE;

        long months = ChronoUnit.MONTHS.between(start, end) + 1;

        if (months == 1) return PeriodType.MONTH;
        if (months == 3) return PeriodType.QUARTER;
        if (months == 6) return PeriodType.HALF_YEAR;
        if (months == 12) return PeriodType.YEAR;

        throw new TaxDateException(
                "Cannot determine period type for validity period: " + start + " ~ " + end);
    }

    // ==================== Quarter/Month Navigation ====================

    /**
     * Returns the quarter that a monthly period belongs to.
     *
     * @param period the period in {@code "yyyyMM"} format
     * @return the quarter period string (e.g. {@code "2026Q1"})
     * @throws NullPointerException if {@code period} is {@code null}
     * @throws TaxDateException     if the period is not 6 characters long
     */
    public static String getQuarterFromMonth(String period) {
        Objects.requireNonNull(period, "Period must not be null");
        if (period.length() != 6 || !period.chars().allMatch(Character::isDigit)) {
            throw new TaxDateException("Period must be in yyyyMM format, got: " + period);
        }
        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(4, 6));
        int quarter = (month - 1) / 3 + 1;
        return year + "Q" + quarter;
    }

    /**
     * Returns the three monthly periods within a quarter.
     *
     * @param quarterPeriod the quarter period string (e.g. {@code "2026Q1"})
     * @return a list of 3 month strings in {@code "yyyyMM"} format
     * @throws NullPointerException if {@code quarterPeriod} is {@code null}
     * @throws TaxDateException     if the format is invalid
     */
    public static List<String> getMonthsInQuarter(String quarterPeriod) {
        Objects.requireNonNull(quarterPeriod, "Quarter period must not be null");
        if (!quarterPeriod.contains("Q")) {
            throw new TaxDateException("Invalid quarter period format: " + quarterPeriod);
        }
        String year = quarterPeriod.substring(0, 4);
        int q = Integer.parseInt(quarterPeriod.substring(5));
        if (q < 1 || q > 4) {
            throw new TaxDateException("Quarter must be between 1 and 4, got: " + q);
        }
        int startMonth = (q - 1) * 3 + 1;

        List<String> months = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            months.add(year + String.format("%02d", startMonth + i));
        }
        return months;
    }

    /**
     * Returns the previous quarter period.
     *
     * @param period the current quarter period (e.g. {@code "2026Q2"})
     * @return the previous quarter (e.g. {@code "2026Q1"})
     * @throws TaxDateException if the period is not a quarterly period
     */
    public static String getPreviousQuarter(String period) {
        TaxPeriod tp = TaxPeriod.parse(period);
        if (tp.type() != PeriodType.QUARTER) {
            throw new TaxDateException("Not a quarter period: " + period);
        }
        return tp.previous().value();
    }

    /**
     * Returns the next quarter period.
     *
     * @param period the current quarter period (e.g. {@code "2026Q2"})
     * @return the next quarter (e.g. {@code "2026Q3"})
     * @throws TaxDateException if the period is not a quarterly period
     */
    public static String getNextQuarter(String period) {
        TaxPeriod tp = TaxPeriod.parse(period);
        if (tp.type() != PeriodType.QUARTER) {
            throw new TaxDateException("Not a quarter period: " + period);
        }
        return tp.next().value();
    }

    /**
     * Returns the previous monthly period.
     *
     * @param period the current monthly period (e.g. {@code "202602"})
     * @return the previous month (e.g. {@code "202601"})
     * @throws TaxDateException if the period is not a monthly period
     */
    public static String getPreviousMonth(String period) {
        TaxPeriod tp = TaxPeriod.parse(period);
        if (tp.type() != PeriodType.MONTH) {
            throw new TaxDateException("Not a month period: " + period);
        }
        return tp.previous().value();
    }

    /**
     * Returns the next monthly period.
     *
     * @param period the current monthly period (e.g. {@code "202602"})
     * @return the next month (e.g. {@code "202603"})
     * @throws TaxDateException if the period is not a monthly period
     */
    public static String getNextMonth(String period) {
        TaxPeriod tp = TaxPeriod.parse(period);
        if (tp.type() != PeriodType.MONTH) {
            throw new TaxDateException("Not a month period: " + period);
        }
        return tp.next().value();
    }

    // ==================== Full Period Enumeration ====================

    /**
     * Returns all period strings at every granularity level within a date range.
     *
     * <p>Given a start and end date, this method generates:</p>
     * <ul>
     *   <li>All monthly periods ({@code "yyyyMM"})</li>
     *   <li>All quarterly periods ({@code "yyyyQn"}) that fall within the range</li>
     *   <li>All half-yearly periods ({@code "yyyyFH"}, {@code "yyyySH"}) within the range</li>
     *   <li>All annual periods ({@code "yyyy"}) within the range</li>
     * </ul>
     *
     * <p>The returned list is ordered: months first, then quarters, then halves, then years.</p>
     *
     * @param startDate the range start in {@code "yyyy-MM-dd"} format
     * @param endDate   the range end in {@code "yyyy-MM-dd"} format
     * @return a list of period strings at all granularity levels
     */
    public static List<String> getFullPeriodList(String startDate, String endDate) {
        Objects.requireNonNull(startDate, "Start date must not be null");
        Objects.requireNonNull(endDate, "End date must not be null");
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return getFullPeriodList(start, end);
    }

    /**
     * Returns a comprehensive list of all period strings at every granularity level
     * within a date range.
     *
     * @param start the range start date
     * @param end   the range end date
     * @return a list of period strings at all granularity levels
     */
    public static List<String> getFullPeriodList(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        List<String> result = new ArrayList<>();

        // Monthly periods
        YearMonth ym = YearMonth.from(start);
        YearMonth endYm = YearMonth.from(end);
        while (!ym.isAfter(endYm)) {
            result.add(ym.getYear() + String.format("%02d", ym.getMonthValue()));
            ym = ym.plusMonths(1);
        }

        // Quarterly periods
        int startYear = start.getYear();
        int endYear = end.getYear();
        int startQ = (start.getMonthValue() - 1) / 3 + 1;
        int endQ = (end.getMonthValue() - 1) / 3 + 1;

        int currentQ = startYear * 4 + startQ - 1;
        int endQIdx = endYear * 4 + endQ - 1;
        while (currentQ <= endQIdx) {
            int y = currentQ / 4;
            int q = currentQ % 4 + 1;
            result.add(y + "Q" + q);
            currentQ++;
        }

        // A half-year is "fully covered" only if the date range spans all 6 months of that half.
        // For the first half (Jan–Jun): the range must start on or before Jan 1 and end on or after Jun 30.
        // For the second half (Jul–Dec): the range must start on or before Jul 1 and end on or after Dec 31.
        // The conditions below check these boundaries, handling edge cases where the range
        // starts or ends mid-year.
        for (int y = startYear; y <= endYear; y++) {
            boolean coversFH = (y != startYear || start.getMonthValue() <= 1)
                    && (y != endYear || end.getMonthValue() >= 6);
            boolean coversSH = (y != startYear || start.getMonthValue() <= 7)
                    && (y != endYear || end.getMonthValue() >= 12);
            if (coversFH) result.add(y + "FH");
            if (coversSH) result.add(y + "SH");
        }

        // Annual periods
        for (int y = startYear; y <= endYear; y++) {
            result.add(String.valueOf(y));
        }

        return result;
    }

    /**
     * Detects and assembles complete period patterns from a list of monthly periods.
     *
     * <p>Scans the input list for patterns that form complete higher-level periods:</p>
     * <ul>
     *   <li>3 consecutive months in the same quarter → quarter period</li>
     *   <li>6 consecutive months in the same half → half-year period</li>
     *   <li>12 consecutive months in the same year → annual period</li>
     * </ul>
     *
     * <p>For example, given {@code ["202601","202602","202603"]}, the result
     * will include {@code "2026Q1"}.</p>
     *
     * @param monthlyPeriods a list of monthly period strings in {@code "yyyyMM"} format
     * @return a list of assembled higher-level period strings
     */
    public static List<String> assembleCompletePeriods(List<String> monthlyPeriods) {
        Objects.requireNonNull(monthlyPeriods, "Monthly periods must not be null");
        if (monthlyPeriods.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        // Parse all months into YearMonth for easier manipulation
        List<YearMonth> months = new ArrayList<>();
        for (String p : monthlyPeriods) {
            if (p.length() == 6) {
                int y = Integer.parseInt(p.substring(0, 4));
                int m = Integer.parseInt(p.substring(4, 6));
                months.add(YearMonth.of(y, m));
            }
        }
        if (months.isEmpty()) {
            return List.of();
        }

        // Detect complete quarters
        YearMonth first = months.get(0);
        YearMonth last = months.get(months.size() - 1);
        YearMonth current = first;
        while (!current.isAfter(last)) {
            int q = (current.getMonthValue() - 1) / 3 + 1;
            int startMonth = (q - 1) * 3 + 1;
            YearMonth qStart = YearMonth.of(current.getYear(), startMonth);
            YearMonth qEnd = qStart.plusMonths(2);
            if (months.contains(qStart) && months.contains(qStart.plusMonths(1)) && months.contains(qEnd)) {
                result.add(current.getYear() + "Q" + q);
            }
            current = qStart.plusMonths(3);
        }

        // Detect complete half-years
        for (int y = first.getYear(); y <= last.getYear(); y++) {
            boolean allFH = true;
            boolean allSH = true;
            for (int m = 1; m <= 6; m++) {
                if (!months.contains(YearMonth.of(y, m))) {
                    allFH = false;
                    break;
                }
            }
            if (allFH) result.add(y + "FH");
            for (int m = 7; m <= 12; m++) {
                if (!months.contains(YearMonth.of(y, m))) {
                    allSH = false;
                    break;
                }
            }
            if (allSH) result.add(y + "SH");

            // Detect complete year
            if (allFH && allSH) {
                result.add(String.valueOf(y));
            }
        }

        return result;
    }

    /**
     * Returns the previous quarter period string from a monthly period.
     *
     * <p>For example, {@code "202605"} (May 2026) → {@code "2026Q1"}
     * (the quarter containing March is Q1, and the previous complete quarter is Q1).</p>
     *
     * @param monthPeriod the monthly period in {@code "yyyyMM"} format
     * @return the previous quarter period string
     */
    public static String getLastQuarter(String monthPeriod) {
        Objects.requireNonNull(monthPeriod, "Month period must not be null");
        if (monthPeriod.length() != 6) {
            throw new TaxDateException("Month period must be in yyyyMM format, got: " + monthPeriod);
        }
        int year = Integer.parseInt(monthPeriod.substring(0, 4));
        int month = Integer.parseInt(monthPeriod.substring(4, 6));
        int currentQ = (month - 1) / 3 + 1;
        if (currentQ == 1) {
            return (year - 1) + "Q4";
        }
        return year + "Q" + (currentQ - 1);
    }

    // ==================== Enhanced Period Type Detection ====================

    /**
     * Determines the {@link PeriodType} from a validity period with tolerance.
     *
     * <p>Unlike {@link #getPeriodTypeByValidity(String, String)} which requires exact
     * month boundaries, this method allows a configurable day tolerance for
     * real-world data where dates may not fall exactly on month boundaries.</p>
     *
     * @param startDate the start date in {@code "yyyy-MM-dd"} format
     * @param endDate   the end date in {@code "yyyy-MM-dd"} format
     * @param toleranceDays the number of days of tolerance allowed (e.g. 2)
     * @return the inferred period type
     * @throws TaxDateException if the type cannot be determined within tolerance
     */
    public static PeriodType getPeriodTypeByValidity(String startDate, String endDate, int toleranceDays) {
        Objects.requireNonNull(startDate, "Start date must not be null");
        Objects.requireNonNull(endDate, "End date must not be null");
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return getPeriodTypeByValidity(start, end, toleranceDays);
    }

    /**
     * Determines the {@link PeriodType} from a validity period with tolerance.
     *
     * @param start         the start date
     * @param end           the end date
     * @param toleranceDays the number of days of tolerance allowed
     * @return the inferred period type
     * @throws TaxDateException if the type cannot be determined within tolerance
     */
    public static PeriodType getPeriodTypeByValidity(LocalDate start, LocalDate end, int toleranceDays) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");

        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        if (totalDays <= 1 + toleranceDays) return PeriodType.ONCE;

        long totalMonths = ChronoUnit.MONTHS.between(start, end) + 1;

        if (Math.abs(totalMonths - 1) <= toleranceDays / PeriodConstants.DAYS_PER_MONTH_APPROX) return PeriodType.MONTH;
        if (Math.abs(totalMonths - 3) <= toleranceDays / PeriodConstants.DAYS_PER_MONTH_APPROX) return PeriodType.QUARTER;
        if (Math.abs(totalMonths - 6) <= toleranceDays / PeriodConstants.DAYS_PER_MONTH_APPROX) return PeriodType.HALF_YEAR;
        if (Math.abs(totalMonths - 12) <= toleranceDays / PeriodConstants.DAYS_PER_MONTH_APPROX) return PeriodType.YEAR;

        throw new TaxDateException(
                "Cannot determine period type for validity period: " + start + " ~ " + end
                        + " (tolerance: " + toleranceDays + " days)");
    }

    /**
     * Returns the months in a half-year period.
     *
     * @param halfYearPeriod the half-year period string (e.g. {@code "2026FH"} or {@code "2026SH"})
     * @return a list of 6 monthly period strings in {@code "yyyyMM"} format
     * @throws TaxDateException if the format is invalid
     */
    public static List<String> getMonthsInHalfYear(String halfYearPeriod) {
        Objects.requireNonNull(halfYearPeriod, "Half-year period must not be null");
        if (!halfYearPeriod.contains("H")) {
            throw new TaxDateException("Invalid half-year period format: " + halfYearPeriod);
        }
        int year = Integer.parseInt(halfYearPeriod.substring(0, 4));
        boolean isFirst = halfYearPeriod.contains("FH");
        int startMonth = isFirst ? 1 : 7;

        List<String> months = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            months.add(year + String.format("%02d", startMonth + i));
        }
        return months;
    }

    /**
     * Returns the months in an annual period.
     *
     * @param yearPeriod the annual period string (e.g. {@code "2026"})
     * @return a list of 12 monthly period strings in {@code "yyyyMM"} format
     */
    public static List<String> getMonthsInYear(String yearPeriod) {
        Objects.requireNonNull(yearPeriod, "Year period must not be null");
        int year = Integer.parseInt(yearPeriod);
        List<String> months = new ArrayList<>(12);
        for (int i = 1; i <= 12; i++) {
            months.add(year + String.format("%02d", i));
        }
        return months;
    }

    /**
     * Returns all quarters in a year as period strings.
     *
     * @param year the calendar year
     * @return a list of 4 quarter strings ({@code "yyyyQ1"} through {@code "yyyyQ4"})
     */
    public static List<String> getQuartersOfYearAsList(int year) {
        return List.of(year + "Q1", year + "Q2", year + "Q3", year + "Q4");
    }

    /**
     * Returns all half-year periods in a year.
     *
     * @param year the calendar year
     * @return a list of 2 half-year strings ({@code "yyyyFH"}, {@code "yyyySH"})
     */
    public static List<String> getHalfYearsOfYear(int year) {
        return List.of(year + "FH", year + "SH");
    }

    // ==================== Private Resolution Methods ====================

    /**
     * Resolves a "yNN" flag to a specific month of the given year.
     *
     * <p>For example, {@code resolveYearMonthFlag("2026", "y03")} returns {@code "202603"}.</p>
     *
     * @param period the base period string (e.g. {@code "2026"})
     * @param flag   the flag string starting with "y" followed by a 1–2 digit month (e.g. {@code "y03"})
     * @return the resolved monthly period string (e.g. {@code "202603"})
     * @throws TaxDateException if the month in the flag is outside 1–12
     */
    private static String resolveYearMonthFlag(String period, String flag) {
        int year = Integer.parseInt(period.substring(0, 4));
        int targetMonth = Integer.parseInt(flag.substring(1));
        if (targetMonth < 1 || targetMonth > 12) {
            throw new TaxDateException("Month in flag must be between 1 and 12, got: " + targetMonth);
        }
        return year + String.format("%02d", targetMonth);
    }

    /**
     * Resolves a "qN" flag to a specific quarter of the given year.
     *
     * <p>For example, {@code resolveQuarterFlag("2026", "q2")} returns {@code "2026Q2"}.</p>
     *
     * @param period the base period string (e.g. {@code "2026"})
     * @param flag   the flag string starting with "q" followed by a single-digit quarter (e.g. {@code "q2"})
     * @return the resolved quarterly period string (e.g. {@code "2026Q2"})
     * @throws TaxDateException if the quarter in the flag is outside 1–4
     */
    private static String resolveQuarterFlag(String period, String flag) {
        int year = Integer.parseInt(period.substring(0, 4));
        int quarter = Integer.parseInt(flag.substring(1));
        if (quarter < 1 || quarter > 4) {
            throw new TaxDateException("Quarter in flag must be between 1 and 4, got: " + quarter);
        }
        return year + "Q" + quarter;
    }

    /**
     * Resolves a correction period by flag code.
     *
     * <ul>
     *   <li>Flag {@code "0"} — strips the correction suffix entirely, returning the base period
     *       (e.g. {@code "202601G2"} → {@code "202601"})</li>
     *   <li>Flag {@code "11"} — decrements the correction sequence number
     *       (e.g. {@code "202601G2"} → {@code "202601G1"}, {@code "202601G1"} → {@code "202601"})</li>
     *   <li>Any other flag — returns the period unchanged</li>
     * </ul>
     *
     * @param period the correction period to resolve
     * @param flag   the flag code ("0", "11", or other)
     * @return the resolved period string
     */
    private static String resolveCorrectionPeriod(TaxPeriod period, String flag) {
        return switch (flag) {
            case "0" -> {
                // Strip the correction suffix to get the base period
                int gIndex = period.value().lastIndexOf('G');
                yield period.value().substring(0, gIndex);
            }
            case "11" -> {
                // Decrement the correction sequence
                int gIndex = period.value().lastIndexOf('G');
                String base = period.value().substring(0, gIndex);
                String suffix = period.value().substring(gIndex);
                if ("G1".equals(suffix)) {
                    // G1 has no previous correction; return the base period
                    yield base;
                }
                int num = Integer.parseInt(suffix.substring(1));
                yield base + "G" + (num - 1);
            }
            default -> period.value();
        };
    }

    /**
     * Resolves a quarterly period by flag code.
     *
     * <table>
     *   <caption>Quarterly flag codes</caption>
     *   <tr><th>Flag</th><th>Meaning</th><th>Example (from 2026Q2)</th></tr>
     *   <tr><td>{@code "0"}</td><td>Current quarter</td><td>{@code "2026Q2"}</td></tr>
     *   <tr><td>{@code "1"}</td><td>Previous quarter</td><td>{@code "2026Q1"}</td></tr>
     *   <tr><td>{@code "2"}</td><td>Same quarter last year</td><td>{@code "2025Q2"}</td></tr>
     *   <tr><td>{@code "3"}</td><td>December of last year</td><td>{@code "202512"}</td></tr>
     *   <tr><td>{@code "4"}</td><td>Previous quarter within same year</td><td>{@code "2026Q1"}</td></tr>
     *   <tr><td>{@code "6"}</td><td>Last month of the quarter</td><td>{@code "202606"}</td></tr>
     * </table>
     *
     * @param period the quarterly period to resolve
     * @param flag   the flag code
     * @return the resolved period string
     */
    private static String resolveQuarterPeriod(TaxPeriod period, String flag) {
        int year = period.year();
        int quarter = period.quarter();

        return switch (flag) {
            case "0" -> period.value();
            case "1" -> {
                // Previous quarter
                if (quarter == 1) yield (year - 1) + "Q4";
                yield year + "Q" + (quarter - 1);
            }
            case "2" -> (year - 1) + "Q" + quarter; // Same quarter last year
            case "3" -> (year - 1) + "12";           // December of last year
            case "4" -> year + "Q" + (quarter - 1); // Previous quarter within the same year
            case "6" -> year + String.format("%02d", quarter * 3); // Last month of the quarter
            default -> period.value();
        };
    }

    /**
     * Resolves a half-year period by flag code.
     *
     * <table>
     *   <caption>Half-year flag codes</caption>
     *   <tr><th>Flag</th><th>Meaning</th><th>Example (from 2026FH)</th></tr>
     *   <tr><td>{@code "0"}</td><td>Current half-year</td><td>{@code "2026FH"}</td></tr>
     *   <tr><td>{@code "1"}</td><td>Previous half-year</td><td>{@code "2025SH"}</td></tr>
     *   <tr><td>{@code "2"}</td><td>Same half-year last year</td><td>{@code "2025FH"}</td></tr>
     *   <tr><td>{@code "3"}</td><td>December of last year</td><td>{@code "202512"}</td></tr>
     * </table>
     *
     * @param period the half-year period to resolve
     * @param flag   the flag code
     * @return the resolved period string
     */
    private static String resolveHalfYearPeriod(TaxPeriod period, String flag) {
        int year = period.year();
        boolean isFirst = period.isFirstHalf();

        return switch (flag) {
            case "0" -> period.value();
            case "1" -> isFirst ? (year - 1) + "SH" : year + "FH"; // Previous half-year
            case "2" -> (year - 1) + (isFirst ? "FH" : "SH");      // Same half last year
            case "3" -> (year - 1) + "12";                          // December of last year
            default -> period.value();
        };
    }

    /**
     * Resolves a per-occurrence period by flag code.
     *
     * <table>
     *   <caption>Per-occurrence flag codes</caption>
     *   <tr><th>Flag</th><th>Meaning</th><th>Example (from 202601N1)</th></tr>
     *   <tr><td>{@code "0"}</td><td>Current period</td><td>{@code "202601N1"}</td></tr>
     *   <tr><td>{@code "5"}</td><td>Strip occurrence suffix</td><td>{@code "202601"}</td></tr>
     *   <tr><td>{@code "10"}</td><td>Convert to containing quarter</td><td>{@code "2026Q1"}</td></tr>
     * </table>
     *
     * @param period the per-occurrence period to resolve
     * @param flag   the flag code
     * @return the resolved period string
     */
    private static String resolveOncePeriod(TaxPeriod period, String flag) {
        return switch (flag) {
            case "0" -> period.value();
            case "5" -> {
                // Strip the occurrence suffix to get the base month
                if (period.value().endsWith("N1")) {
                    yield period.value().substring(0, 6);
                }
                yield period.value();
            }
            case "10" -> {
                // Convert to the quarter containing this month
                int y = period.year();
                int m = period.month();
                int q = (m - 1) / 3 + 1;
                yield y + "Q" + q;
            }
            default -> period.value();
        };
    }

    /**
     * Resolves a monthly period by flag code.
     *
     * <table>
     *   <caption>Monthly flag codes</caption>
     *   <tr><th>Flag</th><th>Meaning</th><th>Example (from 202603)</th></tr>
     *   <tr><td>{@code "0"}</td><td>Current month</td><td>{@code "202603"}</td></tr>
     *   <tr><td>{@code "1"}</td><td>Previous month</td><td>{@code "202602"}</td></tr>
     *   <tr><td>{@code "2"}</td><td>Same month last year</td><td>{@code "202503"}</td></tr>
     *   <tr><td>{@code "3"}</td><td>December of last year</td><td>{@code "202512"}</td></tr>
     *   <tr><td>{@code "4"}</td><td>Previous month within same year</td><td>{@code "202602"}</td></tr>
     *   <tr><td>{@code "9"}</td><td>Tag as first occurrence</td><td>{@code "202603N1"}</td></tr>
     * </table>
     *
     * @param period the monthly period to resolve
     * @param flag   the flag code
     * @return the resolved period string
     */
    private static String resolveMonthPeriod(TaxPeriod period, String flag) {
        int year = period.year();
        int month = period.month();

        return switch (flag) {
            case "0" -> period.value();
            case "1" -> {
                // Previous month
                if (month == 1) yield (year - 1) + "12";
                yield year + String.format("%02d", month - 1);
            }
            case "2" -> (year - 1) + String.format("%02d", month); // Same month last year
            case "3" -> (year - 1) + "12";                          // December of last year
            case "4" -> year + String.format("%02d", month - 1); // Previous month within the same year
            case "9" -> period.value() + "N1"; // Tag as first occurrence
            default -> period.value();
        };
    }

    /**
     * Resolves an annual period by flag code.
     *
     * <table>
     *   <caption>Annual flag codes</caption>
     *   <tr><th>Flag</th><th>Meaning</th><th>Example (from 2026)</th></tr>
     *   <tr><td>{@code "0"}</td><td>Current year</td><td>{@code "2026"}</td></tr>
     *   <tr><td>{@code "1"}, {@code "2"}</td><td>Previous year</td><td>{@code "2025"}</td></tr>
     *   <tr><td>{@code "3"}</td><td>December of last year</td><td>{@code "202512"}</td></tr>
     *   <tr><td>{@code "4"}</td><td>Not supported for annual periods</td><td>throws exception</td></tr>
     *   <tr><td>{@code "7"}</td><td>December of this year</td><td>{@code "202612"}</td></tr>
     *   <tr><td>{@code "8"}</td><td>Q4 of this year</td><td>{@code "2026Q4"}</td></tr>
     * </table>
     *
     * @param period the annual period to resolve
     * @param flag   the flag code
     * @return the resolved period string
     * @throws TaxDateException if flag "4" is used (not supported for annual periods)
     */
    private static String resolveYearPeriod(TaxPeriod period, String flag) {
        int year = period.year();

        return switch (flag) {
            case "0" -> period.value();
            case "1", "2" -> String.valueOf(year - 1); // Previous year
            case "3" -> (year - 1) + "12";             // December of last year
            case "4" -> throw new TaxDateException(
                    "Flag \"4\" (previous within current year) is not supported for annual periods");
            case "7" -> period.value() + "12";          // December of this year
            case "8" -> period.value() + "Q4";          // Q4 of this year
            default -> period.value();
        };
    }
}
