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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the enum constants: {@link PeriodType}, {@link PeriodFlag}.
 */
@DisplayName("Enum constants")
class EnumTest {

    // ==================== PeriodType ====================

    @Nested
    @DisplayName("PeriodType")
    class PeriodTypeTest {

        @Test
        @DisplayName("all values have correct codes")
        void codes() {
            assertEquals("1", PeriodType.MONTH.getCode());
            assertEquals("2", PeriodType.QUARTER.getCode());
            assertEquals("3", PeriodType.HALF_YEAR.getCode());
            assertEquals("4", PeriodType.YEAR.getCode());
            assertEquals("5", PeriodType.ONCE.getCode());
        }

        @Test
        @DisplayName("all values have non-empty descriptions")
        void descriptions() {
            for (PeriodType type : PeriodType.values()) {
                assertNotNull(type.getDescription());
                assertFalse(type.getDescription().isEmpty());
            }
        }

        @Test
        @DisplayName("has exactly 5 values")
        void valueCount() {
            assertEquals(5, PeriodType.values().length);
        }
    }

    // ==================== PeriodFlag ====================

    @Nested
    @DisplayName("PeriodFlag")
    class PeriodFlagTest {

        @Test
        @DisplayName("all values have correct codes")
        void codes() {
            assertEquals("0", PeriodFlag.CURRENT.getCode());
            assertEquals("1", PeriodFlag.PREVIOUS.getCode());
            assertEquals("2", PeriodFlag.SAME_PERIOD_LAST_YEAR.getCode());
            assertEquals("11", PeriodFlag.ONCE_PREVIOUS.getCode());
        }

        @Test
        @DisplayName("fromCode returns the correct flag")
        void fromCode() {
            assertEquals(PeriodFlag.CURRENT, PeriodFlag.fromCode("0"));
            assertEquals(PeriodFlag.PREVIOUS, PeriodFlag.fromCode("1"));
            assertEquals(PeriodFlag.SAME_PERIOD_LAST_YEAR, PeriodFlag.fromCode("2"));
            assertEquals(PeriodFlag.ONCE_PREVIOUS, PeriodFlag.fromCode("11"));
        }

        @Test
        @DisplayName("fromCode throws for unknown code")
        void fromCode_unknown() {
            assertThrows(IllegalArgumentException.class, () -> PeriodFlag.fromCode("99"));
            assertThrows(IllegalArgumentException.class, () -> PeriodFlag.fromCode("abc"));
        }

        @Test
        @DisplayName("has exactly 12 values")
        void valueCount() {
            assertEquals(12, PeriodFlag.values().length);
        }
    }
}
