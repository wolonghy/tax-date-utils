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

import com.wolonghy.taxdate.exception.TaxDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeclareRuleEngine")
class DeclareRuleEngineTest {

    // ==================== Forward: Declare Month → Report Period ====================

    @Nested
    @DisplayName("generateReportPeriods")
    class GenerateReportPeriods {

        @Nested
        @DisplayName("MONTH cycle")
        class MonthlyCycle {

            @Test
            @DisplayName("non-January declaration → previous month")
            void normalMonth() {
                DeclareRuleConfig config = DeclareRuleConfig.defaultMonthly();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202603");
                assertEquals(1, result.size());
                DeclareRulePeriodWrapper wrapper = result.get(0);
                assertEquals("202602", wrapper.getReportPeriod());
                assertEquals(LocalDate.of(2026, 2, 1), wrapper.getTaxPeriodStart());
                assertEquals(LocalDate.of(2026, 2, 28), wrapper.getTaxPeriodEnd());
                assertEquals("2026", wrapper.getDeclareYear());
                assertEquals(List.of("202603"), wrapper.getDeclareMonthList());
            }

            @Test
            @DisplayName("January declaration → December of previous year")
            void january() {
                DeclareRuleConfig config = DeclareRuleConfig.defaultMonthly();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202601");
                assertEquals(1, result.size());
                assertEquals("202512", result.get(0).getReportPeriod());
                assertEquals(LocalDate.of(2025, 12, 1), result.get(0).getTaxPeriodStart());
                assertEquals(LocalDate.of(2025, 12, 31), result.get(0).getTaxPeriodEnd());
            }
        }

        @Nested
        @DisplayName("QUARTER cycle")
        class QuarterlyCycle {

            @Test
            @DisplayName("report previous quarter (default)")
            void previousQuarter() {
                DeclareRuleConfig config = DeclareRuleConfig.defaultQuarterly();
                // April → Q1
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202604");
                assertEquals(1, result.size());
                assertEquals("2026Q1", result.get(0).getReportPeriod());
                assertEquals(LocalDate.of(2026, 1, 1), result.get(0).getTaxPeriodStart());
                assertEquals(LocalDate.of(2026, 3, 31), result.get(0).getTaxPeriodEnd());
            }

            @Test
            @DisplayName("Q1 declaration → Q4 of previous year")
            void q1Declaration() {
                DeclareRuleConfig config = DeclareRuleConfig.defaultQuarterly();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202601");
                assertEquals("2025Q4", result.get(0).getReportPeriod());
            }

            @Test
            @DisplayName("report current quarter")
            void currentQuarter() {
                DeclareRuleConfig config = DeclareRuleConfig.builder()
                        .cycle(DeclareCycle.QUARTER)
                        .reportCurrentQuarter(true)
                        .quarterReportOffset(0)
                        .build();
                // January with reportCurrentQuarter → Q1
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202601");
                assertEquals("2026Q1", result.get(0).getReportPeriod());
            }

            @Test
            @DisplayName("report current quarter with offset")
            void currentQuarterWithOffset() {
                DeclareRuleConfig config = DeclareRuleConfig.builder()
                        .cycle(DeclareCycle.QUARTER)
                        .reportCurrentQuarter(true)
                        .quarterReportOffset(2)
                        .build();
                // March (offset 2 from Q1 start) → Q1
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202603");
                assertEquals("2026Q1", result.get(0).getReportPeriod());
            }
        }

        @Nested
        @DisplayName("FIRST_HALF cycle")
        class FirstHalfCycle {

            @Test
            @DisplayName("July declaration → first half of same year")
            void july() {
                DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.FIRST_HALF).build();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202607");
                assertEquals(1, result.size());
                assertEquals("2026FH", result.get(0).getReportPeriod());
                assertEquals(LocalDate.of(2026, 1, 1), result.get(0).getTaxPeriodStart());
                assertEquals(LocalDate.of(2026, 6, 30), result.get(0).getTaxPeriodEnd());
            }

            @Test
            @DisplayName("non-July declaration → empty list")
            void nonJuly() {
                DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.FIRST_HALF).build();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202608");
                assertTrue(result.isEmpty());
            }
        }

        @Nested
        @DisplayName("SECOND_HALF cycle")
        class SecondHalfCycle {

            @Test
            @DisplayName("January declaration → second half of previous year")
            void january() {
                DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.SECOND_HALF).build();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202601");
                assertEquals(1, result.size());
                assertEquals("2025SH", result.get(0).getReportPeriod());
                assertEquals(LocalDate.of(2025, 7, 1), result.get(0).getTaxPeriodStart());
                assertEquals(LocalDate.of(2025, 12, 31), result.get(0).getTaxPeriodEnd());
            }

            @Test
            @DisplayName("non-January declaration → empty list")
            void nonJanuary() {
                DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.SECOND_HALF).build();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202602");
                assertTrue(result.isEmpty());
            }
        }

        @Nested
        @DisplayName("YEAR cycle")
        class AnnualCycle {

            @Test
            @DisplayName("PREVIOUS_YEAR type → report previous year")
            void previousYear() {
                DeclareRuleConfig config = DeclareRuleConfig.defaultAnnual();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202601");
                assertEquals(1, result.size());
                assertEquals("2025", result.get(0).getReportPeriod());
                assertEquals(LocalDate.of(2025, 1, 1), result.get(0).getTaxPeriodStart());
                assertEquals(LocalDate.of(2025, 12, 31), result.get(0).getTaxPeriodEnd());
            }

            @Test
            @DisplayName("CURRENT_YEAR type → report current year")
            void currentYear() {
                DeclareRuleConfig config = DeclareRuleConfig.builder()
                        .cycle(DeclareCycle.YEAR)
                        .yearType(DeclareYearType.CURRENT_YEAR)
                        .build();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202601");
                assertEquals("2026", result.get(0).getReportPeriod());
            }

            @Test
            @DisplayName("non-January without specificMonths → empty")
            void nonJanuary() {
                DeclareRuleConfig config = DeclareRuleConfig.defaultAnnual();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202603");
                assertTrue(result.isEmpty());
            }

            @Test
            @DisplayName("specificMonths filters declaration months")
            void specificMonths() {
                DeclareRuleConfig config = DeclareRuleConfig.builder()
                        .cycle(DeclareCycle.YEAR)
                        .yearType(DeclareYearType.PREVIOUS_YEAR)
                        .specificMonths(List.of(3, 9))
                        .build();
                // March matches
                assertEquals(1, DeclareRuleEngine.generateReportPeriods(config, "202603").size());
                // April does not match
                assertTrue(DeclareRuleEngine.generateReportPeriods(config, "202604").isEmpty());
            }
        }

        @Nested
        @DisplayName("ONCE cycle")
        class OnceCycle {

            @Test
            @DisplayName("same month declaration")
            void sameMonth() {
                DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.ONCE).build();
                List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.generateReportPeriods(config, "202605");
                assertEquals(1, result.size());
                assertEquals("202605N1", result.get(0).getReportPeriod());
            }
        }

        @Nested
        @DisplayName("Validation")
        class Validation {

            @Test
            @DisplayName("null config throws NullPointerException")
            void nullConfig() {
                assertThrows(NullPointerException.class,
                        () -> DeclareRuleEngine.generateReportPeriods(null, "202601"));
            }

            @Test
            @DisplayName("null declareMonth throws NullPointerException")
            void nullDeclareMonth() {
                assertThrows(NullPointerException.class,
                        () -> DeclareRuleEngine.generateReportPeriods(DeclareRuleConfig.defaultMonthly(), null));
            }

            @Test
            @DisplayName("invalid declareMonth format throws TaxDateException")
            void invalidFormat() {
                assertThrows(TaxDateException.class,
                        () -> DeclareRuleEngine.generateReportPeriods(DeclareRuleConfig.defaultMonthly(), "2026"));
            }
        }
    }

    // ==================== Reverse: Report Period → Declare Months ====================

    @Nested
    @DisplayName("generateDeclareMonths")
    class GenerateDeclareMonths {

        @Test
        @DisplayName("MONTH → next month")
        void monthly() {
            DeclareRuleConfig config = DeclareRuleConfig.defaultMonthly();
            List<String> result = DeclareRuleEngine.generateDeclareMonths(config, "202601");
            assertEquals(List.of("202602"), result);
        }

        @Test
        @DisplayName("MONTH December → January of next year")
        void monthlyDecember() {
            DeclareRuleConfig config = DeclareRuleConfig.defaultMonthly();
            List<String> result = DeclareRuleEngine.generateDeclareMonths(config, "202612");
            assertEquals(List.of("202701"), result);
        }

        @Test
        @DisplayName("QUARTER → month after quarter ends")
        void quarterly() {
            DeclareRuleConfig config = DeclareRuleConfig.defaultQuarterly();
            assertEquals(List.of("202604"), DeclareRuleEngine.generateDeclareMonths(config, "2026Q1"));
            assertEquals(List.of("202607"), DeclareRuleEngine.generateDeclareMonths(config, "2026Q2"));
            assertEquals(List.of("202610"), DeclareRuleEngine.generateDeclareMonths(config, "2026Q3"));
        }

        @Test
        @DisplayName("QUARTER Q4 → January of next year")
        void quarterlyQ4() {
            DeclareRuleConfig config = DeclareRuleConfig.defaultQuarterly();
            assertEquals(List.of("202701"), DeclareRuleEngine.generateDeclareMonths(config, "2026Q4"));
        }

        @Test
        @DisplayName("QUARTER with reportCurrentQuarter and offset")
        void quarterlyCurrentWithOffset() {
            DeclareRuleConfig config = DeclareRuleConfig.builder()
                    .cycle(DeclareCycle.QUARTER)
                    .reportCurrentQuarter(true)
                    .quarterReportOffset(1)
                    .build();
            // Q1 with offset 1 → trigger month = (1-1)*3+1+1 = 2
            assertEquals(List.of("202602"), DeclareRuleEngine.generateDeclareMonths(config, "2026Q1"));
        }

        @Test
        @DisplayName("QUARTER with invalid offset throws TaxDateException")
        void quarterlyInvalidOffset() {
            DeclareRuleConfig config = DeclareRuleConfig.builder()
                    .cycle(DeclareCycle.QUARTER)
                    .reportCurrentQuarter(true)
                    .quarterReportOffset(10)
                    .build();
            assertThrows(TaxDateException.class,
                    () -> DeclareRuleEngine.generateDeclareMonths(config, "2026Q4"));
        }

        @Test
        @DisplayName("FIRST_HALF → July")
        void firstHalf() {
            DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.FIRST_HALF).build();
            assertEquals(List.of("202607"), DeclareRuleEngine.generateDeclareMonths(config, "2026FH"));
        }

        @Test
        @DisplayName("SECOND_HALF → January of next year")
        void secondHalf() {
            DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.SECOND_HALF).build();
            assertEquals(List.of("202701"), DeclareRuleEngine.generateDeclareMonths(config, "2026SH"));
        }

        @Test
        @DisplayName("YEAR PREVIOUS_YEAR → January of next year")
        void annualPreviousYear() {
            DeclareRuleConfig config = DeclareRuleConfig.defaultAnnual();
            assertEquals(List.of("202701"), DeclareRuleEngine.generateDeclareMonths(config, "2026"));
        }

        @Test
        @DisplayName("YEAR with specificMonths")
        void annualSpecificMonths() {
            DeclareRuleConfig config = DeclareRuleConfig.builder()
                    .cycle(DeclareCycle.YEAR)
                    .yearType(DeclareYearType.PREVIOUS_YEAR)
                    .specificMonths(List.of(3, 9))
                    .build();
            assertEquals(List.of("202703", "202709"), DeclareRuleEngine.generateDeclareMonths(config, "2026"));
        }

        @Test
        @DisplayName("ONCE → same month")
        void once() {
            DeclareRuleConfig config = DeclareRuleConfig.builder().cycle(DeclareCycle.ONCE).build();
            assertEquals(List.of("202605"), DeclareRuleEngine.generateDeclareMonths(config, "202605N1"));
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfig() {
            assertThrows(NullPointerException.class,
                    () -> DeclareRuleEngine.generateDeclareMonths(null, "202601"));
        }

        @Test
        @DisplayName("null reportPeriod throws NullPointerException")
        void nullReportPeriod() {
            assertThrows(NullPointerException.class,
                    () -> DeclareRuleEngine.generateDeclareMonths(DeclareRuleConfig.defaultMonthly(), null));
        }
    }

    // ==================== processTaxPeriod ====================

    @Nested
    @DisplayName("processTaxPeriod")
    class ProcessTaxPeriod {

        @Test
        @DisplayName("without special config uses common config")
        void withoutSpecial() {
            DeclareRuleConfig common = DeclareRuleConfig.defaultMonthly();
            List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.processTaxPeriod(common, null, "202603");
            assertEquals(1, result.size());
            assertEquals("202602", result.get(0).getReportPeriod());
        }

        @Test
        @DisplayName("with special config uses special config")
        void withSpecial() {
            DeclareRuleConfig common = DeclareRuleConfig.defaultMonthly();
            DeclareRuleConfig special = DeclareRuleConfig.builder().cycle(DeclareCycle.QUARTER).build();
            List<DeclareRulePeriodWrapper> result = DeclareRuleEngine.processTaxPeriod(common, special, "202604");
            assertEquals(1, result.size());
            assertEquals("2026Q1", result.get(0).getReportPeriod());
        }
    }
}
