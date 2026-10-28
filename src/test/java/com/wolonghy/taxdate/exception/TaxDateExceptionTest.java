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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TaxDateException}.
 */
@DisplayName("TaxDateException")
class TaxDateExceptionTest {

    @Test
    @DisplayName("message constructor sets the message")
    void messageConstructor() {
        TaxDateException ex = new TaxDateException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("message+cause constructor sets both")
    void messageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root cause");
        TaxDateException ex = new TaxDateException("test error", cause);
        assertEquals("test error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("is a RuntimeException (unchecked)")
    void isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new TaxDateException("test"));
    }
}
