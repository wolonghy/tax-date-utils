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
 * Custom unchecked exceptions for the tax-date-utils library.
 *
 * <p>This package provides a single exception type used consistently across all library APIs:</p>
 * <ul>
 *   <li>{@link com.wolonghy.taxdate.exception.TaxDateException} — thrown for invalid period strings,
 *       unparseable date formats, unsupported operations (e.g., converting a YEAR-type period to {@code YearMonth}),
 *       or out-of-range values (month 13, quarter 5, etc.).</li>
 * </ul>
 *
 * <p>All methods in this library throw {@code TaxDateException} (a subclass of {@link RuntimeException})
 * rather than checked exceptions, keeping API signatures clean and allowing callers to handle errors
 * at their discretion.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
package com.wolonghy.taxdate.exception;
