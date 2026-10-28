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
 * Enums and constants for tax period classification, flag-based navigation, and declaration scheduling.
 *
 * <p>This package provides the foundational types used throughout the library to represent
 * period granularity, relative period offsets, and declaration cycle frequencies.</p>
 *
 * <h2>Key enums</h2>
 * <ul>
 *   <li>{@link com.wolonghy.taxdate.constant.PeriodType} — classifies period granularity: MONTH, QUARTER, HALF_YEAR, YEAR, ONCE</li>
 *   <li>{@link com.wolonghy.taxdate.constant.PeriodFlag} — 12 flag codes (0–11) for relative period navigation (current, previous, same period last year, etc.)</li>
 *   <li>{@link com.wolonghy.taxdate.constant.QuickPeriod} — convenient shortcuts for common period queries (CURRENT_MONTH, PREVIOUS_QUARTER, etc.)</li>
* </ul>
 *
 * <h2>Constants</h2>
 * <ul>
 *   <li>{@link com.wolonghy.taxdate.constant.PeriodConstants} — centralized magic numbers (format lengths, month/quarter counts, tolerance values)</li>
 * </ul>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
package com.wolonghy.taxdate.constant;
