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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeclareCycle / DeclareYearType")
class DeclareCycleTest {

    @Nested
    @DisplayName("DeclareCycle.fromCode()")
    class FromCode {

        @Test
        @DisplayName("all codes map correctly")
        void allCodes() {
            assertEquals(DeclareCycle.MONTH, DeclareCycle.fromCode("0"));
            assertEquals(DeclareCycle.QUARTER, DeclareCycle.fromCode("1"));
            assertEquals(DeclareCycle.FIRST_HALF, DeclareCycle.fromCode("2"));
            assertEquals(DeclareCycle.SECOND_HALF, DeclareCycle.fromCode("3"));
            assertEquals(DeclareCycle.YEAR, DeclareCycle.fromCode("4"));
            assertEquals(DeclareCycle.ONCE, DeclareCycle.fromCode("5"));
        }

        @Test
        @DisplayName("null code throws IllegalArgumentException")
        void nullCode() {
            assertThrows(IllegalArgumentException.class, () -> DeclareCycle.fromCode(null));
        }

        @Test
        @DisplayName("unknown code throws IllegalArgumentException")
        void unknownCode() {
            assertThrows(IllegalArgumentException.class, () -> DeclareCycle.fromCode("99"));
        }
    }

    @Nested
    @DisplayName("DeclareCycle.fromPeriodString()")
    class FromPeriodString {

        @Test
        @DisplayName("detects quarterly periods")
        void quarter() {
            assertEquals(DeclareCycle.QUARTER, DeclareCycle.fromPeriodString("2026Q1"));
        }

        @Test
        @DisplayName("detects first-half periods")
        void firstHalf() {
            assertEquals(DeclareCycle.FIRST_HALF, DeclareCycle.fromPeriodString("2026FH"));
        }

        @Test
        @DisplayName("detects second-half periods")
        void secondHalf() {
            assertEquals(DeclareCycle.SECOND_HALF, DeclareCycle.fromPeriodString("2026SH"));
        }

        @Test
        @DisplayName("detects once periods")
        void once() {
            assertEquals(DeclareCycle.ONCE, DeclareCycle.fromPeriodString("202601N1"));
        }

        @Test
        @DisplayName("detects annual periods")
        void year() {
            assertEquals(DeclareCycle.YEAR, DeclareCycle.fromPeriodString("2026"));
        }

        @Test
        @DisplayName("detects monthly periods")
        void month() {
            assertEquals(DeclareCycle.MONTH, DeclareCycle.fromPeriodString("202601"));
        }

        @Test
        @DisplayName("null throws IllegalArgumentException")
        void nullInput() {
            assertThrows(IllegalArgumentException.class, () -> DeclareCycle.fromPeriodString(null));
        }

        @Test
        @DisplayName("blank throws IllegalArgumentException")
        void blankInput() {
            assertThrows(IllegalArgumentException.class, () -> DeclareCycle.fromPeriodString(""));
        }

        @Test
        @DisplayName("unrecognized format throws IllegalArgumentException")
        void unrecognized() {
            assertThrows(IllegalArgumentException.class, () -> DeclareCycle.fromPeriodString("abc"));
            assertThrows(IllegalArgumentException.class, () -> DeclareCycle.fromPeriodString("2026XX"));
        }
    }

    @Nested
    @DisplayName("DeclareYearType.fromCode()")
    class YearTypeFromCode {

        @Test
        @DisplayName("all codes map correctly")
        void allCodes() {
            assertEquals(DeclareYearType.CURRENT_YEAR, DeclareYearType.fromCode("T_C"));
            assertEquals(DeclareYearType.PREVIOUS_YEAR, DeclareYearType.fromCode("T_L"));
        }

        @Test
        @DisplayName("null code throws IllegalArgumentException")
        void nullCode() {
            assertThrows(IllegalArgumentException.class, () -> DeclareYearType.fromCode(null));
        }

        @Test
        @DisplayName("unknown code throws IllegalArgumentException")
        void unknownCode() {
            assertThrows(IllegalArgumentException.class, () -> DeclareYearType.fromCode("X"));
        }
    }
}
