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
 * Enumerates the flag codes used to navigate between related tax periods.
 *
 * <p>Period flags express relative period references such as "previous period",
 * "same period last year", or "last month of quarter". They are used by
 * {@link com.wolonghy.taxdate.period.TaxPeriodResolver#getTargetPeriod(String, String)}
 * to resolve a target period from a base period and a flag.</p>
 *
 * @see #fromCode(String)
 * @see com.wolonghy.taxdate.period.TaxPeriodResolver
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public enum PeriodFlag {

    /** The current period itself. Code: {@code "0"}. */
    CURRENT("0", "Current period"),

    /** The immediately preceding period. Code: {@code "1"}. */
    PREVIOUS("1", "Previous period"),

    /** The same period in the previous year. Code: {@code "2"}. */
    SAME_PERIOD_LAST_YEAR("2", "Same period last year"),

    /** The end of last year (December). Code: {@code "3"}. */
    LAST_YEAR_END("3", "End of last year"),

    /** The previous period within the current year. Code: {@code "4"}. */
    CURRENT_YEAR_PREVIOUS("4", "Previous period within current year"),

    /** The first occurrence of a month. Code: {@code "5"}. */
    MONTH_FIRST("5", "First occurrence of month"),

    /** The last month of the quarter. Code: {@code "6"}. */
    QUARTER_END_MONTH("6", "Last month of quarter"),

    /** The last month of the year (December). Code: {@code "7"}. */
    YEAR_END_MONTH("7", "Last month of year"),

    /** The last quarter of the year (Q4). Code: {@code "8"}. */
    YEAR_END_QUARTER("8", "Last quarter of year"),

    /** First occurrence tag for a month period. Code: {@code "9"}. */
    MONTH_FIRST_OCCURRENCE("9", "First occurrence tag for month"),

    /** The current quarter for a per-occurrence period. Code: {@code "10"}. */
    ONCE_CURRENT_QUARTER("10", "Current quarter for per-occurrence"),

    /** The previous occurrence. Code: {@code "11"}. */
    ONCE_PREVIOUS("11", "Previous occurrence");

    private final String code;
    private final String description;

    PeriodFlag(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the string code identifying this flag (e.g. {@code "0"} for CURRENT).
     *
     * @return the flag code, never {@code null}
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns a human-readable description of this flag.
     *
     * @return the description, never {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Looks up a {@code PeriodFlag} by its string code.
     *
     * @param code the flag code to look up (e.g. {@code "0"}, {@code "2"}, {@code "11"})
     * @return the matching {@code PeriodFlag}
     * @throws IllegalArgumentException if no flag matches the given code
     */
    public static PeriodFlag fromCode(String code) {
        for (PeriodFlag flag : values()) {
            if (flag.code.equals(code)) {
                return flag;
            }
        }
        throw new IllegalArgumentException("Unknown period flag code: " + code);
    }
}
