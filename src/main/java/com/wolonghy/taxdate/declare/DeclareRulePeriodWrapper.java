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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Holds the result of a declaration period calculation.
 *
 * <p>When generating report periods from a declaration month (or vice versa),
 * the result includes the report period string, the tax period's start and end
 * dates, the declaration year, and the list of declaration months.</p>
 *
 * <h3>Example</h3>
 * <p>For a quarterly tax type with declaration month {@code "202604"}:</p>
 * <pre>{@code
 * reportPeriod      = "2026Q1"
 * taxPeriodStart    = 2026-01-01
 * taxPeriodEnd      = 2026-03-31
 * declareYear       = "2026"
 * declareMonthList  = ["202604"]
 * }</pre>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class DeclareRulePeriodWrapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String reportPeriod;
    private final LocalDate taxPeriodStart;
    private final LocalDate taxPeriodEnd;
    private final String declareYear;
    private final List<String> declareMonthList;

    /**
     * Constructs a new wrapper.
     *
     * @param reportPeriod     the report period string (e.g. {@code "202601"}, {@code "2026Q1"})
     * @param taxPeriodStart   the tax period start date
     * @param taxPeriodEnd     the tax period end date
     * @param declareYear      the declaration year string
     * @param declareMonthList the list of declaration months ({@code "yyyyMM"})
     */
    public DeclareRulePeriodWrapper(String reportPeriod,
                                     LocalDate taxPeriodStart,
                                     LocalDate taxPeriodEnd,
                                     String declareYear,
                                     List<String> declareMonthList) {
        this.reportPeriod = reportPeriod;
        this.taxPeriodStart = taxPeriodStart;
        this.taxPeriodEnd = taxPeriodEnd;
        this.declareYear = declareYear;
        this.declareMonthList = declareMonthList != null
                ? Collections.unmodifiableList(new ArrayList<>(declareMonthList))
                : Collections.emptyList();
    }

    /**
     * Returns the report period string (e.g. {@code "202601"}, {@code "2026Q1"}).
     *
     * @return the report period
     */
    public String getReportPeriod() {
        return reportPeriod;
    }

    /**
     * Returns the tax period start date (inclusive).
     *
     * @return the first day of the tax period
     */
    public LocalDate getTaxPeriodStart() {
        return taxPeriodStart;
    }

    /**
     * Returns the tax period end date (inclusive).
     *
     * @return the last day of the tax period
     */
    public LocalDate getTaxPeriodEnd() {
        return taxPeriodEnd;
    }

    /**
     * Returns the declaration year as a string (e.g. {@code "2026"}).
     *
     * @return the declaration year
     */
    public String getDeclareYear() {
        return declareYear;
    }

    /**
     * Returns the list of declaration months in {@code "yyyyMM"} format.
     *
     * <p>For most taxes this is a single-element list, but some special
     * rules may produce multiple declaration months for one report period.</p>
     *
     * @return an unmodifiable list of month strings
     */
    public List<String> getDeclareMonthList() {
        return declareMonthList;
    }

    /**
     * Returns the tax period start date formatted as {@code "yyyy-MM-dd"}.
     *
     * @return the formatted start date string
     */
    public String getTaxPeriodStartStr() {
        return taxPeriodStart != null ? taxPeriodStart.format(DATE_FMT) : null;
    }

    /**
     * Returns the tax period end date formatted as {@code "yyyy-MM-dd"}.
     *
     * @return the formatted end date string
     */
    public String getTaxPeriodEndStr() {
        return taxPeriodEnd != null ? taxPeriodEnd.format(DATE_FMT) : null;
    }

    @Override
    public String toString() {
        return "DeclareRulePeriodWrapper{" +
                "reportPeriod='" + reportPeriod + '\'' +
                ", taxPeriodStart=" + taxPeriodStart +
                ", taxPeriodEnd=" + taxPeriodEnd +
                ", declareYear='" + declareYear + '\'' +
                ", declareMonthList=" + declareMonthList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeclareRulePeriodWrapper that)) return false;
        return Objects.equals(reportPeriod, that.reportPeriod)
                && Objects.equals(taxPeriodStart, that.taxPeriodStart)
                && Objects.equals(taxPeriodEnd, that.taxPeriodEnd)
                && Objects.equals(declareYear, that.declareYear)
                && Objects.equals(declareMonthList, that.declareMonthList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportPeriod, taxPeriodStart, taxPeriodEnd, declareYear, declareMonthList);
    }
}
