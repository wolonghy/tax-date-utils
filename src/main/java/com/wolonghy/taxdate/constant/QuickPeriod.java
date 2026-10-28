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
 * Enumerates the quick period types for deriving a period string from the current date.
 *
 * <p>Each {@code QuickPeriod} constant represents a specific period granularity.
 * When passed to
 * {@link com.wolonghy.taxdate.period.TaxPeriodResolver#resolveQuickPeriod(QuickPeriod)},
 * the method computes the corresponding period string based on the current system date.</p>
 *
 * <table>
 *   <caption>Quick period output formats</caption>
 *   <tr><th>Constant</th><th>Output format</th><th>Example (current date: 2026-10-28)</th></tr>
 *   <tr><td>{@link #CURRENT_MONTH}</td><td>{@code "yyyyMM"}</td><td>{@code "202610"}</td></tr>
 *   <tr><td>{@link #PREVIOUS_MONTH}</td><td>{@code "yyyyMM"}</td><td>{@code "202609"}</td></tr>
 *   <tr><td>{@link #CURRENT_QUARTER}</td><td>{@code "yyyyQn"}</td><td>{@code "2026Q4"}</td></tr>
 *   <tr><td>{@link #PREVIOUS_YEAR}</td><td>{@code "yyyy"}</td><td>{@code "2025"}</td></tr>
 *   <tr><td>{@link #FIRST_HALF}</td><td>{@code "yyyyFH"}</td><td>{@code "2026FH"}</td></tr>
 *   <tr><td>{@link #SECOND_HALF}</td><td>{@code "yyyySH"}</td><td>{@code "2026SH"}</td></tr>
 *   <tr><td>{@link #CURRENT_MONTH_OCCURRENCE}</td><td>{@code "yyyyMMN1"}</td><td>{@code "202610N1"}</td></tr>
 * </table>
 *
 * @see com.wolonghy.taxdate.period.TaxPeriodResolver#resolveQuickPeriod(QuickPeriod)
 *
 * @author Zhang Ze
 * @since 1.2.0
 */
public enum QuickPeriod {

    /** The current month (e.g. {@code "202610"}). */
    CURRENT_MONTH,

    /** The previous month (e.g. {@code "202609"}). */
    PREVIOUS_MONTH,

    /** The current quarter (e.g. {@code "2026Q4"}). */
    CURRENT_QUARTER,

    /** The previous year (e.g. {@code "2025"}). */
    PREVIOUS_YEAR,

    /** The first half of the current year (e.g. {@code "2026FH"}). */
    FIRST_HALF,

    /** The second half of the current year (e.g. {@code "2026SH"}). */
    SECOND_HALF,

    /** The current month as a per-occurrence period (e.g. {@code "202610N1"}). */
    CURRENT_MONTH_OCCURRENCE
}

