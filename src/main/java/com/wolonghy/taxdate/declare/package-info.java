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
 * Declaration rule engine for computing tax report periods and filing months.
 *
 * <p>This package provides a rule-driven approach to the two core computations
 * in tax declaration scheduling:</p>
 * <ul>
 *   <li>Given a declaration month, determine which report period(s) to file</li>
 *   <li>Given a report period, determine in which month(s) it must be filed</li>
 * </ul>
 *
 * <h2>Key classes</h2>
 * <ul>
 *   <li>{@link com.wolonghy.taxdate.declare.DeclareRuleEngine} — main entry point for period computation</li>
 *   <li>{@link com.wolonghy.taxdate.declare.DeclareRuleConfig} — builder-pattern configuration for declaration rules</li>
 *   <li>{@link com.wolonghy.taxdate.declare.DeclareRulePeriodWrapper} — result wrapper with report period and tax period dates</li>
 * </ul>
 *
 * <h2>Enums</h2>
 * <ul>
 *   <li>{@link com.wolonghy.taxdate.declare.DeclareCycle} — declaration cycle frequencies (monthly, quarterly, half-yearly, annual, per-occurrence)</li>
 *   <li>{@link com.wolonghy.taxdate.declare.DeclareYearType} — distinguishes current-year from previous-year declarations</li>
 * </ul>
 *
 * <p>The engine supports all standard declaration cycles: monthly, quarterly,
 * half-yearly, annual, and per-occurrence, with configurable rules for special
 * cases like "report current quarter" or specific annual filing months.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
package com.wolonghy.taxdate.declare;
