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
package com.wolonghy.taxdate.exception;

/**
 * Custom unchecked exception thrown by the tax-date-utils library when
 * period parsing, date calculation, or other tax-date operations fail.
 *
 * <p>Extends {@link RuntimeException} so callers are not forced to catch it;
 * it typically indicates invalid input or an unsupported operation.</p>
 *
 * @see com.wolonghy.taxdate.model.TaxPeriod#parse(String)
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public class TaxDateException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public TaxDateException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public TaxDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
