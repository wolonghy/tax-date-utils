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
 * Enumerates the types of tax periods supported by the library.
 *
 * <p>Each type carries a numeric {@linkplain #getCode() code} used for
 * serialization and a human-readable {@linkplain #getDescription() description}.</p>
 *
 * <ul>
 *   <li>{@link #MONTH} — a single calendar month (e.g. {@code 202601})</li>
 *   <li>{@link #QUARTER} — a calendar quarter (e.g. {@code 2026Q1})</li>
 *   <li>{@link #HALF_YEAR} — the first or second half of a year (e.g. {@code 2026FH}, {@code 2026SH})</li>
 *   <li>{@link #YEAR} — a full calendar year (e.g. {@code 2026})</li>
 *   <li>{@link #ONCE} — a per-occurrence period tied to a specific month (e.g. {@code 202601N1})</li>
 * </ul>
 *
 * @see com.wolonghy.taxdate.model.TaxPeriod
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public enum PeriodType {

    /** A single calendar month. Code: {@code "1"}. */
    MONTH("1", "Monthly"),

    /** A calendar quarter (Q1–Q4). Code: {@code "2"}. */
    QUARTER("2", "Quarterly"),

    /** The first or second half of a year. Code: {@code "3"}. */
    HALF_YEAR("3", "Semi-annual"),

    /** A full calendar year. Code: {@code "4"}. */
    YEAR("4", "Annual"),

    /** A per-occurrence period. Code: {@code "5"}. */
    ONCE("5", "Per-occurrence");

    private final String code;
    private final String description;

    PeriodType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the numeric code identifying this period type (e.g. {@code "1"} for MONTH).
     *
     * @return the period type code, never {@code null}
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns a human-readable description of this period type.
     *
     * @return the description, never {@code null}
     */
    public String getDescription() {
        return description;
    }
}
