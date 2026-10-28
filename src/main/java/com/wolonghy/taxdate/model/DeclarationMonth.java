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

package com.wolonghy.taxdate.model;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Represents the month in which a tax period must be declared (filed).
 *
 * <p>For most period types the declaration month is the month <em>after</em>
 * the period ends. For example, a monthly period {@code 202601} (January 2026)
 * is declared in {@code DeclarationMonth(2026, 2)} (February 2026).</p>
 *
 * <p>Special cases:</p>
 * <ul>
 *   <li>December monthly periods are declared in January of the <em>next</em> year</li>
 *   <li>Q4 quarterly periods are declared in January of the next year</li>
 *   <li>Annual periods are declared in January of the next year</li>
 * </ul>
 *
 * @param year  the declaration year
 * @param month the declaration month (1–12)
 *
 * @see com.wolonghy.taxdate.period.TaxPeriodResolver#getDeclarationMonth(String)
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public record DeclarationMonth(int year, int month) {

    /**
     * Compact constructor that validates the month range.
     *
     * @throws IllegalArgumentException if {@code month} is not between 1 and 12
     */
    public DeclarationMonth {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
    }

    /**
     * Returns the month as a zero-padded two-digit string (e.g. {@code "01"}, {@code "12"}).
     *
     * @return the formatted month string
     */
    public String formattedMonth() {
        return String.format("%02d", month);
    }

    /**
     * Returns the declaration month in {@code "yyyy-MM"} format (e.g. {@code "2026-02"}).
     *
     * @return the dash-formatted year-month string
     */
    public String formattedMonthDash() {
        return year + "-" + String.format("%02d", month);
    }

    /**
     * Converts this declaration month to a {@link YearMonth}.
     *
     * @return the corresponding {@code YearMonth}
     */
    public YearMonth toYearMonth() {
        return YearMonth.of(year, month);
    }

    /**
     * Returns the declaration month in {@code yyyyMM} format (e.g. {@code "202602"}).
     *
     * @return the formatted year-month string
     */
    @Override
    public String toString() {
        return year + formattedMonth();
    }
}
