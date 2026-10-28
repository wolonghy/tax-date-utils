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
 * Indicates whether an annual tax period reports the current year or the previous year.
 *
 * <p>Some taxes are reported in the same year they apply (e.g. property tax),
 * while others are reported in the following year (e.g. annual income tax).</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public enum DeclareYearType {

    /** Current year reports current year (当年报当年, T_C). */
    CURRENT_YEAR("T_C", "Current year reports current year"),

    /** Current year reports previous year (次年报上年, T_L). */
    PREVIOUS_YEAR("T_L", "Next year reports previous year");

    private final String code;
    private final String description;

    DeclareYearType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the type code.
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
     * Resolves a {@code DeclareYearType} from its code.
     *
     * @param code the code string ({@code "T_C"} or {@code "T_L"})
     * @return the matching type
     * @throws IllegalArgumentException if the code is not recognized
     */
    public static DeclareYearType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Declare year type code must not be null");
        }
        return switch (code) {
            case "T_C" -> CURRENT_YEAR;
            case "T_L" -> PREVIOUS_YEAR;
            default -> throw new IllegalArgumentException("Unknown declare year type code: " + code);
        };
    }
}
