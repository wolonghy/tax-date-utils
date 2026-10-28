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

import com.wolonghy.taxdate.constant.PeriodType;
import com.wolonghy.taxdate.exception.TaxDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TaxPeriod} — the core domain model of the library.
 */
@DisplayName("TaxPeriod")
class TaxPeriodTest {

    // ==================== Factory Methods ====================

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("ofMonth creates a monthly period with correct value and fields")
        void ofMonth_validInput() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 1);

            assertEquals("202601", period.value());
            assertEquals(PeriodType.MONTH, period.type());
            assertEquals(2026, period.year());
            assertEquals(1, period.month());
            assertNull(period.quarter());
            assertNull(period.correctionSequence());
        }

        @Test
        @DisplayName("ofMonth pads single-digit months with zero")
        void ofMonth_singleDigitMonth() {
            assertEquals("202603", TaxPeriod.ofMonth(2026, 3).value());
            assertEquals("202612", TaxPeriod.ofMonth(2026, 12).value());
        }

        @Test
        @DisplayName("ofMonth throws for invalid month")
        void ofMonth_invalidMonth() {
            assertThrows(IllegalArgumentException.class, () -> TaxPeriod.ofMonth(2026, 0));
            assertThrows(IllegalArgumentException.class, () -> TaxPeriod.ofMonth(2026, 13));
        }

        @Test
        @DisplayName("ofMonth(YearMonth) creates a monthly period from YearMonth")
        void ofMonth_yearMonth() {
            TaxPeriod period = TaxPeriod.ofMonth(YearMonth.of(2026, 3));
            assertEquals("202603", period.value());
            assertEquals(PeriodType.MONTH, period.type());
            assertEquals(2026, period.year());
            assertEquals(3, period.month());
        }

        @Test
        @DisplayName("ofMonth(YearMonth) throws NullPointerException for null")
        void ofMonth_yearMonth_null() {
            assertThrows(NullPointerException.class, () -> TaxPeriod.ofMonth((YearMonth) null));
        }

        @Test
        @DisplayName("ofQuarter creates a quarterly period")
        void ofQuarter_validInput() {
            TaxPeriod period = TaxPeriod.ofQuarter(2026, 2);

            assertEquals("2026Q2", period.value());
            assertEquals(PeriodType.QUARTER, period.type());
            assertEquals(2026, period.year());
            assertEquals(2, period.quarter());
            assertNull(period.month());
        }

        @Test
        @DisplayName("ofQuarter throws for invalid quarter")
        void ofQuarter_invalidQuarter() {
            assertThrows(IllegalArgumentException.class, () -> TaxPeriod.ofQuarter(2026, 0));
            assertThrows(IllegalArgumentException.class, () -> TaxPeriod.ofQuarter(2026, 5));
        }

        @Test
        @DisplayName("ofHalfYear creates first-half and second-half periods")
        void ofHalfYear() {
            TaxPeriod fh = TaxPeriod.ofHalfYear(2026, true);
            TaxPeriod sh = TaxPeriod.ofHalfYear(2026, false);

            assertEquals("2026FH", fh.value());
            assertEquals(PeriodType.HALF_YEAR, fh.type());
            assertTrue(fh.isFirstHalf());
            assertFalse(fh.isSecondHalf());

            assertEquals("2026SH", sh.value());
            assertTrue(sh.isSecondHalf());
            assertFalse(sh.isFirstHalf());
        }

        @Test
        @DisplayName("ofYear creates an annual period")
        void ofYear() {
            TaxPeriod period = TaxPeriod.ofYear(2026);

            assertEquals("2026", period.value());
            assertEquals(PeriodType.YEAR, period.type());
            assertEquals(2026, period.year());
        }

        @Test
        @DisplayName("ofOnce creates a per-occurrence period")
        void ofOnce() {
            TaxPeriod period = TaxPeriod.ofOnce(2026, 3);

            assertEquals("202603N1", period.value());
            assertEquals(PeriodType.ONCE, period.type());
            assertEquals(2026, period.year());
            assertEquals(3, period.month());
        }

        @Test
        @DisplayName("ofOnce throws for invalid month")
        void ofOnce_invalidMonth() {
            assertThrows(IllegalArgumentException.class, () -> TaxPeriod.ofOnce(2026, 0));
            assertThrows(IllegalArgumentException.class, () -> TaxPeriod.ofOnce(2026, 13));
        }
    }

    // ==================== Parsing ====================

    @Nested
    @DisplayName("parse()")
    class Parsing {

        @Test
        @DisplayName("parses monthly period")
        void parse_month() {
            TaxPeriod period = TaxPeriod.parse("202601");
            assertEquals(PeriodType.MONTH, period.type());
            assertEquals(2026, period.year());
            assertEquals(1, period.month());
        }

        @Test
        @DisplayName("parses quarterly period")
        void parse_quarter() {
            TaxPeriod period = TaxPeriod.parse("2026Q3");
            assertEquals(PeriodType.QUARTER, period.type());
            assertEquals(2026, period.year());
            assertEquals(3, period.quarter());
        }

        @Test
        @DisplayName("parses first-half period")
        void parse_firstHalf() {
            TaxPeriod period = TaxPeriod.parse("2026FH");
            assertEquals(PeriodType.HALF_YEAR, period.type());
            assertTrue(period.isFirstHalf());
        }

        @Test
        @DisplayName("parses second-half period")
        void parse_secondHalf() {
            TaxPeriod period = TaxPeriod.parse("2026SH");
            assertEquals(PeriodType.HALF_YEAR, period.type());
            assertTrue(period.isSecondHalf());
        }

        @Test
        @DisplayName("parses annual period")
        void parse_year() {
            TaxPeriod period = TaxPeriod.parse("2026");
            assertEquals(PeriodType.YEAR, period.type());
            assertEquals(2026, period.year());
        }

        @Test
        @DisplayName("parses per-occurrence period")
        void parse_once() {
            TaxPeriod period = TaxPeriod.parse("202601N1");
            assertEquals(PeriodType.ONCE, period.type());
            assertEquals(1, period.month());
        }

        @Test
        @DisplayName("parses correction period and extracts sequence number")
        void parse_correction() {
            TaxPeriod period = TaxPeriod.parse("202601G2");
            assertEquals(PeriodType.MONTH, period.type());
            assertTrue(period.isCorrection());
            assertEquals(2, period.correctionSequence());
            assertEquals(2026, period.year());
            assertEquals(1, period.month());
        }

        @Test
        @DisplayName("parses correction period with default sequence 1")
        void parse_correction_defaultSeq() {
            TaxPeriod period = TaxPeriod.parse("202601G");
            assertTrue(period.isCorrection());
            assertEquals(1, period.correctionSequence());
        }

        @Test
        @DisplayName("parse trims whitespace")
        void parse_trimsWhitespace() {
            TaxPeriod period = TaxPeriod.parse("  202601  ");
            assertEquals("202601", period.value());
        }

        @Test
        @DisplayName("parse throws for null input")
        void parse_null() {
            assertThrows(NullPointerException.class, () -> TaxPeriod.parse(null));
        }

        @Test
        @DisplayName("parse throws for blank input")
        void parse_blank() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse(""));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("   "));
        }

        @Test
        @DisplayName("parse throws for unrecognized format")
        void parse_unrecognized() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("abc"));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("20261"));  // 5 digits
        }

        @Test
        @DisplayName("parse throws for half-year with invalid suffix")
        void parse_halfYearInvalidSuffix() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("2026HH"));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("2026H1"));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("2026XH"));
        }

        @Test
        @DisplayName("parse throws for out-of-range month")
        void parse_outOfRangeMonth() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202613")); // month 13
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202600")); // month 0
        }

        @Test
        @DisplayName("parse throws for out-of-range quarter")
        void parse_outOfRangeQuarter() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("2026Q5")); // quarter 5
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("2026Q0")); // quarter 0
        }

        @Test
        @DisplayName("parse throws for quarter period too short")
        void parse_quarterTooShort() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("2026Q"));
        }

        @Test
        @DisplayName("parse throws for once period too short")
        void parse_onceTooShort() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("20260N")); // 6 chars, need 7
        }

        @Test
        @DisplayName("parse throws for once period with out-of-range month")
        void parse_onceOutOfRangeMonth() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202613N1"));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202600N1"));
        }

        @Test
        @DisplayName("parse throws for correction with invalid sequence")
        void parse_correctionInvalidSequence() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202601G0"));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202601G-1"));
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("202601Gabc"));
        }

        @Test
        @DisplayName("parse throws for correction with G at start")
        void parse_correctionGAtStart() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.parse("G202601"));
        }

        @Test
        @DisplayName("parse correction for quarterly period")
        void parse_correctionQuarterly() {
            TaxPeriod period = TaxPeriod.parse("2026Q1G3");
            assertEquals(PeriodType.QUARTER, period.type());
            assertTrue(period.isCorrection());
            assertEquals(3, period.correctionSequence());
            assertEquals(2026, period.year());
            assertEquals(1, period.quarter());
        }

        @Test
        @DisplayName("parse correction for half-year period")
        void parse_correctionHalfYear() {
            TaxPeriod period = TaxPeriod.parse("2026FHG2");
            assertEquals(PeriodType.HALF_YEAR, period.type());
            assertTrue(period.isCorrection());
            assertEquals(2, period.correctionSequence());
            assertTrue(period.isFirstHalf());
        }

        @Test
        @DisplayName("parse correction for annual period")
        void parse_correctionAnnual() {
            TaxPeriod period = TaxPeriod.parse("2026G1");
            assertEquals(PeriodType.YEAR, period.type());
            assertTrue(period.isCorrection());
            assertEquals(1, period.correctionSequence());
            assertEquals(2026, period.year());
        }
    }

    // ==================== Query Methods ====================

    @Nested
    @DisplayName("Query methods")
    class QueryMethods {

        @Test
        @DisplayName("isCorrection returns true only for correction periods")
        void isCorrection() {
            assertTrue(TaxPeriod.parse("202601G1").isCorrection());
            assertTrue(TaxPeriod.parse("2026Q1G2").isCorrection());
            assertFalse(TaxPeriod.parse("202601").isCorrection());
            assertFalse(TaxPeriod.parse("2026Q1").isCorrection());
        }

        @Test
        @DisplayName("toYearMonth works for MONTH and ONCE types")
        void toYearMonth_validTypes() {
            assertEquals(YearMonth.of(2026, 3), TaxPeriod.ofMonth(2026, 3).toYearMonth());
            assertEquals(YearMonth.of(2026, 7), TaxPeriod.ofOnce(2026, 7).toYearMonth());
        }

        @Test
        @DisplayName("toYearMonth throws for types without month component")
        void toYearMonth_invalidType() {
            assertThrows(TaxDateException.class, () -> TaxPeriod.ofYear(2026).toYearMonth());
            assertThrows(TaxDateException.class, () -> TaxPeriod.ofQuarter(2026, 1).toYearMonth());
            assertThrows(TaxDateException.class, () -> TaxPeriod.ofHalfYear(2026, true).toYearMonth());
        }

        @Test
        @DisplayName("record toString includes all fields")
        void recordToString() {
            String str = TaxPeriod.ofMonth(2026, 1).toString();
            assertTrue(str.contains("value=202601"));
            assertTrue(str.contains("type=MONTH"));
            assertTrue(str.contains("year=2026"));
            assertTrue(str.contains("month=1"));
        }

        @Test
        @DisplayName("isFirstHalf returns false for non-half-year types")
        void isFirstHalf_nonHalfYear() {
            assertFalse(TaxPeriod.ofMonth(2026, 3).isFirstHalf());
            assertFalse(TaxPeriod.ofQuarter(2026, 1).isFirstHalf());
            assertFalse(TaxPeriod.ofYear(2026).isFirstHalf());
            assertFalse(TaxPeriod.ofOnce(2026, 5).isFirstHalf());
        }

        @Test
        @DisplayName("isSecondHalf returns false for non-half-year types")
        void isSecondHalf_nonHalfYear() {
            assertFalse(TaxPeriod.ofMonth(2026, 3).isSecondHalf());
            assertFalse(TaxPeriod.ofQuarter(2026, 1).isSecondHalf());
            assertFalse(TaxPeriod.ofYear(2026).isSecondHalf());
            assertFalse(TaxPeriod.ofOnce(2026, 5).isSecondHalf());
        }
    }

    // ==================== toDateRange ====================

    @Nested
    @DisplayName("toDateRange()")
    class ToDateRange {

        @Test
        @DisplayName("monthly period covers the full month")
        void month_january() {
            DateRange range = TaxPeriod.ofMonth(2026, 1).toDateRange();
            assertEquals(LocalDate.of(2026, 1, 1), range.start());
            assertEquals(LocalDate.of(2026, 1, 31), range.end());
        }

        @Test
        @DisplayName("monthly period handles February in leap year")
        void month_february_leapYear() {
            DateRange range = TaxPeriod.ofMonth(2024, 2).toDateRange();
            assertEquals(LocalDate.of(2024, 2, 1), range.start());
            assertEquals(LocalDate.of(2024, 2, 29), range.end());
        }

        @Test
        @DisplayName("monthly period handles February in non-leap year")
        void month_february_nonLeapYear() {
            DateRange range = TaxPeriod.ofMonth(2026, 2).toDateRange();
            assertEquals(LocalDate.of(2026, 2, 28), range.end());
        }

        @Test
        @DisplayName("quarterly period covers three months")
        void quarter() {
            DateRange q1 = TaxPeriod.ofQuarter(2026, 1).toDateRange();
            assertEquals(LocalDate.of(2026, 1, 1), q1.start());
            assertEquals(LocalDate.of(2026, 3, 31), q1.end());

            DateRange q3 = TaxPeriod.ofQuarter(2026, 3).toDateRange();
            assertEquals(LocalDate.of(2026, 7, 1), q3.start());
            assertEquals(LocalDate.of(2026, 9, 30), q3.end());
        }

        @Test
        @DisplayName("half-year periods cover six months each")
        void halfYear() {
            DateRange fh = TaxPeriod.ofHalfYear(2026, true).toDateRange();
            assertEquals(LocalDate.of(2026, 1, 1), fh.start());
            assertEquals(LocalDate.of(2026, 6, 30), fh.end());

            DateRange sh = TaxPeriod.ofHalfYear(2026, false).toDateRange();
            assertEquals(LocalDate.of(2026, 7, 1), sh.start());
            assertEquals(LocalDate.of(2026, 12, 31), sh.end());
        }

        @Test
        @DisplayName("annual period covers the full year")
        void year() {
            DateRange range = TaxPeriod.ofYear(2026).toDateRange();
            assertEquals(LocalDate.of(2026, 1, 1), range.start());
            assertEquals(LocalDate.of(2026, 12, 31), range.end());
        }

        @Test
        @DisplayName("once period covers a single day (today)")
        void once() {
            LocalDate today = LocalDate.now();
            DateRange range = TaxPeriod.ofOnce(2026, 5).toDateRange(today);
            assertEquals(range.start(), range.end());
            assertEquals(today, range.start());
        }

        @Test
        @DisplayName("toDateRange(LocalDate) for non-ONCE types ignores the date parameter")
        void toDateRange_withDate_nonOnce() {
            LocalDate anyDate = LocalDate.of(2026, 6, 15);
            
            DateRange monthRange = TaxPeriod.ofMonth(2026, 3).toDateRange(anyDate);
            assertEquals(LocalDate.of(2026, 3, 1), monthRange.start());
            assertEquals(LocalDate.of(2026, 3, 31), monthRange.end());
            
            DateRange quarterRange = TaxPeriod.ofQuarter(2026, 2).toDateRange(anyDate);
            assertEquals(LocalDate.of(2026, 4, 1), quarterRange.start());
            assertEquals(LocalDate.of(2026, 6, 30), quarterRange.end());
            
            DateRange yearRange = TaxPeriod.ofYear(2026).toDateRange(anyDate);
            assertEquals(LocalDate.of(2026, 1, 1), yearRange.start());
            assertEquals(LocalDate.of(2026, 12, 31), yearRange.end());
        }

        @Test
        @DisplayName("toDateRange(LocalDate) throws NullPointerException for null date")
        void toDateRange_withDate_nullDate() {
            TaxPeriod period = TaxPeriod.ofOnce(2026, 5);
            assertThrows(NullPointerException.class, () -> period.toDateRange(null));
        }

        @Test
        @DisplayName("toDateRange() throws for ONCE type without date parameter")
        void toDateRange_onceWithoutDate_throws() {
            TaxPeriod period = TaxPeriod.ofOnce(2026, 5);
            assertThrows(TaxDateException.class, () -> period.toDateRange());
        }
    }

    // ==================== Navigation ====================

    @Nested
    @DisplayName("Navigation")
    class Navigation {

        @Test
        @DisplayName("previous() moves to the prior month")
        void previous_month() {
            assertEquals("202512", TaxPeriod.ofMonth(2026, 1).previous().value());
            assertEquals("202605", TaxPeriod.ofMonth(2026, 6).previous().value());
        }

        @Test
        @DisplayName("next() moves to the following month")
        void next_month() {
            assertEquals("202701", TaxPeriod.ofMonth(2026, 12).next().value());
            assertEquals("202607", TaxPeriod.ofMonth(2026, 6).next().value());
        }

        @Test
        @DisplayName("previous() moves to the prior quarter")
        void previous_quarter() {
            assertEquals("2025Q4", TaxPeriod.ofQuarter(2026, 1).previous().value());
            assertEquals("2026Q1", TaxPeriod.ofQuarter(2026, 2).previous().value());
        }

        @Test
        @DisplayName("next() moves to the following quarter")
        void next_quarter() {
            assertEquals("2027Q1", TaxPeriod.ofQuarter(2026, 4).next().value());
            assertEquals("2026Q3", TaxPeriod.ofQuarter(2026, 2).next().value());
        }

        @Test
        @DisplayName("previous/next for half-year periods")
        void halfYear_navigation() {
            // SH → FH (same year)
            assertEquals("2026FH", TaxPeriod.ofHalfYear(2026, false).previous().value());
            // FH → SH (previous year)
            assertEquals("2025SH", TaxPeriod.ofHalfYear(2026, true).previous().value());
            // FH → SH (same year)
            assertEquals("2026SH", TaxPeriod.ofHalfYear(2026, true).next().value());
            // SH → FH (next year)
            assertEquals("2027FH", TaxPeriod.ofHalfYear(2026, false).next().value());
        }

        @Test
        @DisplayName("previous/next for year periods")
        void year_navigation() {
            assertEquals("2025", TaxPeriod.ofYear(2026).previous().value());
            assertEquals("2027", TaxPeriod.ofYear(2026).next().value());
        }

        @Test
        @DisplayName("previous/next for ONCE returns self")
        void once_navigation_returnsSelf() {
            TaxPeriod once = TaxPeriod.ofOnce(2026, 5);
            assertSame(once, once.previous());
            assertSame(once, once.next());
        }

        @Test
        @DisplayName("samePeriodLastYear works for all types")
        void samePeriodLastYear() {
            assertEquals("202501", TaxPeriod.ofMonth(2026, 1).samePeriodLastYear().value());
            assertEquals("2025Q2", TaxPeriod.ofQuarter(2026, 2).samePeriodLastYear().value());
            assertEquals("2025FH", TaxPeriod.ofHalfYear(2026, true).samePeriodLastYear().value());
            assertEquals("2025", TaxPeriod.ofYear(2026).samePeriodLastYear().value());
            assertEquals("202503N1", TaxPeriod.ofOnce(2026, 3).samePeriodLastYear().value());
        }
    }

    // ==================== Constructor Validation ====================

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("null value throws NullPointerException")
        void nullValue() {
            assertThrows(NullPointerException.class,
                    () -> new TaxPeriod(null, PeriodType.MONTH, 2026, null, 1, null));
        }

        @Test
        @DisplayName("blank value throws TaxDateException")
        void blankValue() {
            assertThrows(TaxDateException.class,
                    () -> new TaxPeriod("  ", PeriodType.MONTH, 2026, null, 1, null));
        }
    }

    // ==================== Record Equality ====================

    @Nested
    @DisplayName("Record equality")
    class RecordEquality {

        @Test
        @DisplayName("equal periods are equal")
        void equalPeriods() {
            TaxPeriod p1 = TaxPeriod.ofMonth(2026, 3);
            TaxPeriod p2 = TaxPeriod.ofMonth(2026, 3);
            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("different periods are not equal")
        void differentPeriods() {
            TaxPeriod p1 = TaxPeriod.ofMonth(2026, 3);
            TaxPeriod p2 = TaxPeriod.ofMonth(2026, 4);
            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("periods with different types are not equal")
        void differentTypes() {
            TaxPeriod month = TaxPeriod.ofMonth(2026, 1);
            TaxPeriod quarter = TaxPeriod.ofQuarter(2026, 1);
            assertNotEquals(month, quarter);
        }

        @Test
        @DisplayName("correction periods with different sequences are not equal")
        void differentCorrectionSequences() {
            TaxPeriod p1 = TaxPeriod.parse("202601G1");
            TaxPeriod p2 = TaxPeriod.parse("202601G2");
            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("period is not equal to null")
        void notEqualToNull() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 3);
            assertNotEquals(null, period);
        }

        @Test
        @DisplayName("period is not equal to different object type")
        void notEqualToDifferentType() {
            TaxPeriod period = TaxPeriod.ofMonth(2026, 3);
            assertNotEquals("202603", period);
        }
    }
}
