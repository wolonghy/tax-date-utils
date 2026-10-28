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

/**
 * Enumerates the declaration cycle frequencies for tax filing.
 *
 * <p>Each tax type has a declaration cycle that determines how often
 * returns must be filed. This enum provides a standardized representation
 * independent of any specific tax authority's coding scheme.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public enum DeclareCycle {

    /** Monthly declaration (月度). */
    MONTH("MONTH", "Monthly"),

    /** Quarterly declaration (季度). */
    QUARTER("QUARTER", "Quarterly"),

    /** First half-year declaration (上半年度). */
    FIRST_HALF("FIRST_HALF", "First Half-Year"),

    /** Second half-year declaration (下半年度). */
    SECOND_HALF("SECOND_HALF", "Second Half-Year"),

    /** Annual declaration (年度). */
    YEAR("YEAR", "Annual"),

    /** Per-occurrence declaration (按次). */
    ONCE("ONCE", "Per-Occurrence");

    private final String code;
    private final String description;

    DeclareCycle(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the cycle code.
     *
     * @return the code string
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the human-readable description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Resolves a {@code DeclareCycle} from a numeric code string.
     *
     * <p>Mapping: {@code "0"} → MONTH, {@code "1"} → QUARTER,
     * {@code "2"} → FIRST_HALF, {@code "3"} → SECOND_HALF,
     * {@code "4"} → YEAR, {@code "5"} → ONCE.</p>
     *
     * @param code the numeric code
     * @return the matching cycle
     * @throws IllegalArgumentException if the code is not recognized
     */
    public static DeclareCycle fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Declare cycle code must not be null");
        }
        return switch (code) {
            case "0" -> MONTH;
            case "1" -> QUARTER;
            case "2" -> FIRST_HALF;
            case "3" -> SECOND_HALF;
            case "4" -> YEAR;
            case "5" -> ONCE;
            default -> throw new IllegalArgumentException("Unknown declare cycle code: " + code);
        };
    }

    /**
     * Resolves a {@code DeclareCycle} from a period string format.
     *
     * @param period the period string (e.g. {@code "202601"}, {@code "2026Q1"})
     * @return the matching cycle
     */
    public static DeclareCycle fromPeriodString(String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("Period must not be null or blank");
        }
        if (period.contains("Q")) return QUARTER;
        if (period.contains("FH")) return FIRST_HALF;
        if (period.contains("SH")) return SECOND_HALF;
        if (period.contains("N")) return ONCE;
        if (period.length() == 4 && period.chars().allMatch(Character::isDigit)) return YEAR;
        if (period.length() == 6 && period.chars().allMatch(Character::isDigit)) return MONTH;
        throw new IllegalArgumentException("Unrecognized period format: " + period);
    }
}
