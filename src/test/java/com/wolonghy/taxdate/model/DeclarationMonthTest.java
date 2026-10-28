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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DeclarationMonth}.
 */
@DisplayName("DeclarationMonth")
class DeclarationMonthTest {

    @Test
    @DisplayName("valid construction with year and month")
    void validConstruction() {
        DeclarationMonth dm = new DeclarationMonth(2026, 2);
        assertEquals(2026, dm.year());
        assertEquals(2, dm.month());
    }

    @Test
    @DisplayName("formattedMonth pads single-digit month with zero")
    void formattedMonth_padding() {
        assertEquals("01", new DeclarationMonth(2026, 1).formattedMonth());
        assertEquals("02", new DeclarationMonth(2026, 2).formattedMonth());
        assertEquals("12", new DeclarationMonth(2026, 12).formattedMonth());
    }

    @Test
    @DisplayName("toString returns yyyyMM format")
    void toStringFormat() {
        assertEquals("202602", new DeclarationMonth(2026, 2).toString());
        assertEquals("202701", new DeclarationMonth(2027, 1).toString());
    }

    @Test
    @DisplayName("month below 1 throws IllegalArgumentException")
    void invalidMonth_below() {
        assertThrows(IllegalArgumentException.class, () -> new DeclarationMonth(2026, 0));
    }

    @Test
    @DisplayName("month above 12 throws IllegalArgumentException")
    void invalidMonth_above() {
        assertThrows(IllegalArgumentException.class, () -> new DeclarationMonth(2026, 13));
    }

    @Test
    @DisplayName("boundary months 1 and 12 are valid")
    void boundaryMonths() {
        assertDoesNotThrow(() -> new DeclarationMonth(2026, 1));
        assertDoesNotThrow(() -> new DeclarationMonth(2026, 12));
    }

    @Test
    @DisplayName("toYearMonth converts correctly")
    void toYearMonth() {
        DeclarationMonth dm = new DeclarationMonth(2026, 3);
        assertEquals(YearMonth.of(2026, 3), dm.toYearMonth());
    }

    @Test
    @DisplayName("formattedMonthDash returns yyyy-MM format")
    void formattedMonthDash() {
        assertEquals("2026-01", new DeclarationMonth(2026, 1).formattedMonthDash());
        assertEquals("2026-12", new DeclarationMonth(2026, 12).formattedMonthDash());
    }
}
