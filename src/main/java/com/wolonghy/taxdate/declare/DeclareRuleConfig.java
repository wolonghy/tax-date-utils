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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for a tax type's declaration rule.
 *
 * <p>Encapsulates all parameters needed to compute report periods and
 * declaration months for a specific tax type. This replaces the need for
 * multiple scattered configuration flags.</p>
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * DeclareRuleConfig config = DeclareRuleConfig.builder()
 *     .cycle(DeclareCycle.QUARTER)
 *     .yearType(DeclareYearType.CURRENT_YEAR)
 *     .reportCurrentQuarter(true)
 *     .quarterReportOffset(0)
 *     .build();
 * }</pre>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class DeclareRuleConfig {

    private final DeclareCycle cycle;
    private final DeclareYearType yearType;
    private final List<Integer> specificMonths;
    private final boolean reportCurrentQuarter;
    private final int quarterReportOffset;

    private DeclareRuleConfig(Builder builder) {
        this.cycle = Objects.requireNonNull(builder.cycle, "DeclareCycle must not be null");
        this.yearType = builder.yearType != null ? builder.yearType : DeclareYearType.CURRENT_YEAR;
        this.specificMonths = builder.specificMonths != null
                ? Collections.unmodifiableList(builder.specificMonths)
                : Collections.emptyList();
        this.reportCurrentQuarter = builder.reportCurrentQuarter;
        this.quarterReportOffset = builder.quarterReportOffset;
    }

    /** Returns the declaration cycle. */
    public DeclareCycle getCycle() {
        return cycle;
    }

    /** Returns the year type (current year or previous year). */
    public DeclareYearType getYearType() {
        return yearType;
    }

    /** Returns the specific months for annual declarations (e.g. [5, 11]). */
    public List<Integer> getSpecificMonths() {
        return specificMonths;
    }

    /** Returns whether quarterly declarations report the current quarter. */
    public boolean isReportCurrentQuarter() {
        return reportCurrentQuarter;
    }

    /**
     * Returns the quarter report offset.
     *
     * <p>For quarterly declarations, this indicates which month within the quarter
     * triggers the report: 0 = first month, 1 = second month, 2 = third month.</p>
     */
    public int getQuarterReportOffset() {
        return quarterReportOffset;
    }

    /**
     * Creates a new builder for {@code DeclareRuleConfig}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default monthly configuration.
     *
     * @return a config for monthly declaration with current year type
     */
    public static DeclareRuleConfig defaultMonthly() {
        return builder().cycle(DeclareCycle.MONTH).build();
    }

    /**
     * Creates a default quarterly configuration.
     *
     * @return a config for quarterly declaration
     */
    public static DeclareRuleConfig defaultQuarterly() {
        return builder().cycle(DeclareCycle.QUARTER).build();
    }

    /**
     * Creates a default annual configuration (previous year type).
     *
     * @return a config for annual declaration
     */
    public static DeclareRuleConfig defaultAnnual() {
        return builder().cycle(DeclareCycle.YEAR).yearType(DeclareYearType.PREVIOUS_YEAR).build();
    }

    /**
     * Builder for {@code DeclareRuleConfig}.
     */
    public static final class Builder {
        private DeclareCycle cycle;
        private DeclareYearType yearType;
        private List<Integer> specificMonths;
        private boolean reportCurrentQuarter;
        private int quarterReportOffset;

        private Builder() {}

        /**
         * Sets the declaration cycle.
         *
         * @param cycle the cycle (monthly, quarterly, half-yearly, annual, or per-occurrence)
         * @return this builder for method chaining
         */
        public Builder cycle(DeclareCycle cycle) {
            this.cycle = cycle;
            return this;
        }

        /**
         * Sets the year type for annual declarations.
         *
         * @param yearType {@link DeclareYearType#CURRENT_YEAR} or {@link DeclareYearType#PREVIOUS_YEAR}
         * @return this builder for method chaining
         */
        public Builder yearType(DeclareYearType yearType) {
            this.yearType = yearType;
            return this;
        }

        /**
         * Sets the specific months for annual declarations that occur in particular months.
         *
         * <p>For example, some tax types require filing only in May and November each year.
         * Pass {@code List.of(5, 11)} to configure such a rule.</p>
         *
         * @param specificMonths a list of month numbers (1–12), or {@code null} to use all months
         * @return this builder for method chaining
         */
        public Builder specificMonths(List<Integer> specificMonths) {
            this.specificMonths = specificMonths;
            return this;
        }

        /**
         * Sets whether quarterly declarations report the current quarter instead of the previous one.
         *
         * <p>When {@code true}, a declaration in April (Q2) reports Q2 itself rather than Q1.
         * This is useful for tax types with "report-as-you-go" rules.</p>
         *
         * @param reportCurrentQuarter {@code true} to report the current quarter
         * @return this builder for method chaining
         */
        public Builder reportCurrentQuarter(boolean reportCurrentQuarter) {
            this.reportCurrentQuarter = reportCurrentQuarter;
            return this;
        }

        /**
         * Sets the offset within a quarter that triggers the report.
         *
         * <p>For quarterly declarations, this determines which month of the quarter
         * the report is due: {@code 0} = first month, {@code 1} = second month,
         * {@code 2} = third month. The default is {@code 0} (first month).</p>
         *
         * @param quarterReportOffset the offset (0, 1, or 2)
         * @return this builder for method chaining
         */
        public Builder quarterReportOffset(int quarterReportOffset) {
            this.quarterReportOffset = quarterReportOffset;
            return this;
        }

        /**
         * Builds the {@code DeclareRuleConfig} from the configured parameters.
         *
         * @return an immutable {@code DeclareRuleConfig} instance
         * @throws NullPointerException if {@link #cycle(DeclareCycle)} was not called
         */
        public DeclareRuleConfig build() {
            return new DeclareRuleConfig(this);
        }
    }
}
