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

package com.wolonghy.taxdate.declare;

import com.wolonghy.taxdate.constant.PeriodConstants;
import com.wolonghy.taxdate.exception.TaxDateException;
import com.wolonghy.taxdate.model.TaxPeriod;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Engine for computing tax declaration periods and filing months.
 *
 * <p>This is the central class for the declaration rule engine. It replaces
 * ad-hoc period calculation logic scattered across business services with a
 * unified, rule-driven approach.</p>
 *
 * <h3>Core concepts</h3>
 * <ul>
 *   <li><strong>Report period (税款所属期)</strong> — the period the tax applies to
 *       (e.g. {@code "202601"} for January 2026 monthly VAT)</li>
 *   <li><strong>Declaration month (申报月)</strong> — the month in which the return
 *       is filed (e.g. {@code "202602"} for filing January's VAT)</li>
 *   <li><strong>Declare cycle</strong> — the frequency of filing: monthly, quarterly,
 *       half-yearly, annually, or per-occurrence</li>
 * </ul>
 *
 * <h3>Two directions of computation</h3>
 * <ol>
 *   <li>{@link #generateReportPeriods} — given a declaration month, compute which
 *       report period(s) should be filed</li>
 *   <li>{@link #generateDeclareMonths} — given a report period, compute in which
 *       declaration month(s) it should be filed</li>
 * </ol>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class DeclareRuleEngine {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private DeclareRuleEngine() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Forward: Declare Month → Report Period ====================

    /**
     * Generates the report period(s) that should be filed in a given declaration month,
     * using common (default) rules.
     *
     * <p>Common rules assume standard period-to-declaration mapping:</p>
     * <ul>
     *   <li>Monthly ({@link DeclareCycle#MONTH}): declare month M → report period M-1</li>
     *   <li>Quarterly ({@link DeclareCycle#QUARTER}): months 1,4,7,10 → previous quarter</li>
     *   <li>First half ({@link DeclareCycle#FIRST_HALF}): July → first half of same year</li>
     *   <li>Second half ({@link DeclareCycle#SECOND_HALF}): January → second half of previous year</li>
     *   <li>Annual ({@link DeclareCycle#YEAR}): January → previous year</li>
     *   <li>Once ({@link DeclareCycle#ONCE}): same month</li>
     * </ul>
     *
     * @param config       the declaration rule configuration, never {@code null}
     * @param declareMonth the declaration month in {@code "yyyyMM"} format
     * @return a list of period wrappers (usually one element)
     */
    public static List<DeclareRulePeriodWrapper> generateReportPeriods(DeclareRuleConfig config,
                                                                        String declareMonth) {
        Objects.requireNonNull(config, "Config must not be null");
        Objects.requireNonNull(declareMonth, "Declare month must not be null");
        if (declareMonth.length() != PeriodConstants.MONTH_FORMAT_LENGTH
                || !declareMonth.chars().allMatch(Character::isDigit)) {
            throw new TaxDateException("Declare month must be in yyyyMM format, got: " + declareMonth);
        }

        int declareYear = Integer.parseInt(declareMonth.substring(0, 4));
        int declareMon = Integer.parseInt(declareMonth.substring(4, 6));

        return switch (config.getCycle()) {
            case MONTH -> List.of(generateMonthlyReportPeriod(declareYear, declareMon));
            case QUARTER -> List.of(generateQuarterlyReportPeriod(declareYear, declareMon, config));
            case FIRST_HALF -> generateFirstHalfReportPeriod(declareYear, declareMon);
            case SECOND_HALF -> generateSecondHalfReportPeriod(declareYear, declareMon);
            case YEAR -> generateAnnualReportPeriod(declareYear, declareMon, config);
            case ONCE -> List.of(generateOnceReportPeriod(declareYear, declareMon));
        };
    }

    // ==================== Reverse: Report Period → Declare Months ====================

    /**
     * Generates the declaration month(s) for a given report period, using common rules.
     *
     * @param config       the declaration rule configuration
     * @param reportPeriod the report period string (e.g. {@code "202601"}, {@code "2026Q1"})
     * @return a list of declaration months in {@code "yyyyMM"} format
     */
    public static List<String> generateDeclareMonths(DeclareRuleConfig config,
                                                      String reportPeriod) {
        Objects.requireNonNull(config, "Config must not be null");
        Objects.requireNonNull(reportPeriod, "Report period must not be null");

        TaxPeriod period = TaxPeriod.parse(reportPeriod);

        return switch (config.getCycle()) {
            case MONTH -> List.of(generateMonthlyDeclareMonth(period));
            case QUARTER -> generateQuarterlyDeclareMonths(period, config);
            case FIRST_HALF -> List.of(generateFirstHalfDeclareMonth(period));
            case SECOND_HALF -> List.of(generateSecondHalfDeclareMonth(period));
            case YEAR -> generateAnnualDeclareMonths(period, config);
            case ONCE -> generateOnceDeclareMonths(period);
        };
    }

    /**
     * Dispatches to either common or special rule processing based on whether
     * a special declaration rule config is provided.
     *
     * @param commonConfig   the common rule configuration
     * @param specialConfig  the special rule configuration (may be {@code null})
     * @param declareMonth   the declaration month in {@code "yyyyMM"} format
     * @return the list of report period wrappers
     */
    public static List<DeclareRulePeriodWrapper> processTaxPeriod(DeclareRuleConfig commonConfig,
                                                                   DeclareRuleConfig specialConfig,
                                                                   String declareMonth) {
        if (specialConfig != null) {
            return generateReportPeriodsByRule(specialConfig, declareMonth);
        }
        return generateReportPeriods(commonConfig, declareMonth);
    }

    /**
     * Generates report periods using a special declaration rule (e.g. "report current quarter").
     *
     * @param ruleConfig   the declaration rule configuration
     * @param declareMonth the declaration month in {@code "yyyyMM"} format
     * @return the list of period wrappers
     */
    public static List<DeclareRulePeriodWrapper> generateReportPeriodsByRule(DeclareRuleConfig ruleConfig,
                                                                              String declareMonth) {
        return generateReportPeriods(ruleConfig, declareMonth);
    }

    // ==================== Monthly ====================

    private static DeclareRulePeriodWrapper generateMonthlyReportPeriod(int declareYear, int declareMon) {
        YearMonth reportYm;
        int reportYear;
        if (declareMon == 1) {
            reportYm = YearMonth.of(declareYear - 1, 12);
            reportYear = declareYear - 1;
        } else {
            reportYm = YearMonth.of(declareYear, declareMon - 1);
            reportYear = declareYear;
        }
        String reportPeriod = reportYm.format(MONTH_FMT);
        return new DeclareRulePeriodWrapper(
                reportPeriod,
                reportYm.atDay(1),
                reportYm.atEndOfMonth(),
                String.valueOf(reportYear),
                List.of(formatMonth(declareYear, declareMon))
        );
    }

    private static String generateMonthlyDeclareMonth(TaxPeriod period) {
        if (period.month() == 12) {
            return formatMonth(period.year() + 1, 1);
        }
        return formatMonth(period.year(), period.month() + 1);
    }

    // ==================== Quarterly ====================

    private static DeclareRulePeriodWrapper generateQuarterlyReportPeriod(int declareYear, int declareMon,
                                                                           DeclareRuleConfig config) {
        // Determine the current quarter from the declaration month.
        int currentQ = (declareMon - 1) / 3 + 1;

        int reportQ;
        int reportYear;
        if (config.isReportCurrentQuarter()) {
            // Special rule: report the quarter we are currently in.
            reportQ = currentQ;
            reportYear = declareYear;
        } else {
            // Standard rule: report the previous quarter.
            // Q1 rolls back to Q4 of the previous year.
            if (currentQ == 1) {
                reportQ = 4;
                reportYear = declareYear - 1;
            } else {
                reportQ = currentQ - 1;
                reportYear = declareYear;
            }
        }

        int startMonth = (reportQ - 1) * 3 + 1;
        int endMonth = reportQ * 3;
        String reportPeriod = reportYear + "Q" + reportQ;

        return new DeclareRulePeriodWrapper(
                reportPeriod,
                LocalDate.of(reportYear, startMonth, 1),
                LocalDate.of(reportYear, endMonth, 1).with(TemporalAdjusters.lastDayOfMonth()),
                String.valueOf(declareYear),
                List.of(formatMonth(declareYear, declareMon))
        );
    }

    private static List<String> generateQuarterlyDeclareMonths(TaxPeriod period, DeclareRuleConfig config) {
        int q = period.quarter();
        int year = period.year();
        List<String> months = new ArrayList<>();

        if (config.isReportCurrentQuarter()) {
            int offset = config.getQuarterReportOffset();
            int triggerMonth = (q - 1) * 3 + 1 + offset;
            // Validate that triggerMonth is within valid range (1-12)
            if (triggerMonth < 1 || triggerMonth > 12) {
                throw new TaxDateException(
                        "Invalid quarter report offset: trigger month " + triggerMonth +
                        " is out of range [1-12] for Q" + q);
            }
            months.add(formatMonth(year, triggerMonth));
        } else {
            int startDeclareMonth = q * 3 + 1;
            if (q == 4) {
                months.add(formatMonth(year + 1, 1));
            } else {
                months.add(formatMonth(year, startDeclareMonth));
            }
        }
        return months;
    }

    // ==================== First Half ====================

    private static List<DeclareRulePeriodWrapper> generateFirstHalfReportPeriod(int declareYear, int declareMon) {
        if (declareMon != 7) {
            return Collections.emptyList();
        }
        String reportPeriod = declareYear + "FH";
        return List.of(new DeclareRulePeriodWrapper(
                reportPeriod,
                LocalDate.of(declareYear, 1, 1),
                LocalDate.of(declareYear, 6, 30),
                String.valueOf(declareYear),
                List.of(formatMonth(declareYear, 7))
        ));
    }

    private static String generateFirstHalfDeclareMonth(TaxPeriod period) {
        return formatMonth(period.year(), 7);
    }

    // ==================== Second Half ====================

    private static List<DeclareRulePeriodWrapper> generateSecondHalfReportPeriod(int declareYear, int declareMon) {
        if (declareMon != 1) {
            return Collections.emptyList();
        }
        int reportYear = declareYear - 1;
        String reportPeriod = reportYear + "SH";
        return List.of(new DeclareRulePeriodWrapper(
                reportPeriod,
                LocalDate.of(reportYear, 7, 1),
                LocalDate.of(reportYear, 12, 31),
                String.valueOf(reportYear),
                List.of(formatMonth(declareYear, 1))
        ));
    }

    private static String generateSecondHalfDeclareMonth(TaxPeriod period) {
        return formatMonth(period.year() + 1, 1);
    }

    // ==================== Annual ====================

    private static List<DeclareRulePeriodWrapper> generateAnnualReportPeriod(int declareYear, int declareMon,
                                                                              DeclareRuleConfig config) {
        List<Integer> specificMonths = config.getSpecificMonths();
        if (!specificMonths.isEmpty() && !specificMonths.contains(declareMon)) {
            return Collections.emptyList();
        }
        if (specificMonths.isEmpty() && declareMon != 1) {
            return Collections.emptyList();
        }

        int reportYear;
        if (config.getYearType() == DeclareYearType.PREVIOUS_YEAR) {
            reportYear = declareYear - 1;
        } else {
            reportYear = declareYear;
        }

        String reportPeriod = String.valueOf(reportYear);
        return List.of(new DeclareRulePeriodWrapper(
                reportPeriod,
                LocalDate.of(reportYear, 1, 1),
                LocalDate.of(reportYear, 12, 31),
                String.valueOf(reportYear),
                List.of(formatMonth(declareYear, declareMon))
        ));
    }

    private static List<String> generateAnnualDeclareMonths(TaxPeriod period, DeclareRuleConfig config) {
        List<Integer> specificMonths = config.getSpecificMonths();
        int year = period.year();

        if (config.getYearType() == DeclareYearType.PREVIOUS_YEAR) {
            if (!specificMonths.isEmpty()) {
                List<String> months = new ArrayList<>();
                for (int m : specificMonths) {
                    months.add(formatMonth(year + 1, m));
                }
                return months;
            }
            return List.of(formatMonth(year + 1, 1));
        } else {
            if (!specificMonths.isEmpty()) {
                List<String> months = new ArrayList<>();
                for (int m : specificMonths) {
                    months.add(formatMonth(year, m));
                }
                return months;
            }
            return List.of(formatMonth(year, 1));
        }
    }

    // ==================== Once (Per-Occurrence) ====================

    private static DeclareRulePeriodWrapper generateOnceReportPeriod(int declareYear, int declareMon) {
        String reportPeriod = formatMonth(declareYear, declareMon) + "N1";
        YearMonth ym = YearMonth.of(declareYear, declareMon);
        return new DeclareRulePeriodWrapper(
                reportPeriod,
                ym.atDay(1),
                ym.atEndOfMonth(),
                String.valueOf(declareYear),
                List.of(formatMonth(declareYear, declareMon))
        );
    }

    private static List<String> generateOnceDeclareMonths(TaxPeriod period) {
        return List.of(formatMonth(period.year(), period.month()));
    }

    // ==================== Private Helpers ====================

    private static String formatMonth(int year, int month) {
        return year + String.format("%02d", month);
    }
}
