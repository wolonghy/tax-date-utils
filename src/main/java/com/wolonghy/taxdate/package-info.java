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
/**
 * A lightweight Java library for tax date and period handling in financial and tax systems.
 *
 * <p><strong>100% {@code java.time}</strong> — all APIs use {@link java.time.LocalDate},
 * {@link java.time.LocalDateTime}, and {@link java.time.YearMonth}. Zero usage of
 * {@code java.util.Date}, {@code java.util.Calendar}, or legacy date types.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Date formatting, parsing, calculation, and comparison</li>
 *   <li>Tax period modeling (month, quarter, half-year, year, per-occurrence)</li>
 *   <li>Deadline calculation with weekend/holiday adjustment</li>
 *   <li>Declaration rule engine for report period computation</li>
 *   <li>Excel and OA date format conversion</li>
 * </ul>
 *
 * <h2>Package structure</h2>
 * <ul>
 *   <li>{@code constant} — Enums and numeric constants ({@code PeriodType}, {@code PeriodFlag}, {@code PeriodConstants})</li>
 *   <li>{@code model} — Immutable value objects ({@code TaxPeriod}, {@code DateRange}, {@code DeclarationMonth})</li>
 *   <li>{@code period} — Period resolution, navigation, and arithmetic</li>
 *   <li>{@code deadline} — Declaration deadline calculation with weekend/holiday adjustment</li>
 *   <li>{@code declare} — Declaration rule engine for report period computation</li>
 *   <li>{@code converter} — Date format converters (OA date, Excel date)</li>
 *   <li>{@code util} — General-purpose date utilities and SQL expression helpers</li>
 *   <li>{@code exception} — Unified runtime exception ({@code TaxDateException})</li>
 * </ul>
 *
 * <h2>Quick start</h2>
 * <p>New users should begin with these core classes:</p>
 * <ul>
 *   <li>{@link com.wolonghy.taxdate.model.TaxPeriod} — the central domain model for all period types</li>
 *   <li>{@link com.wolonghy.taxdate.period.TaxPeriodResolver} — resolve periods by flag codes and navigate between them</li>
 *   <li>{@link com.wolonghy.taxdate.util.TaxDateUtils} — general-purpose date formatting, parsing, and calculation</li>
 *   <li>{@link com.wolonghy.taxdate.deadline.TaxDeadlineCalculator} — compute filing deadlines with weekend adjustment</li>
 *   <li>{@link com.wolonghy.taxdate.declare.DeclareRuleEngine} — compute report periods from declaration months (and vice versa)</li>
 * </ul>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
package com.wolonghy.taxdate;
