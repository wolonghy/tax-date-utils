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

package com.wolonghy.taxdate.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility methods for week-based date calculations.
 *
 * <p>Provides helpers for computing week boundaries (Monday/Sunday),
 * generating lists of recent days, and other week-related operations
 * commonly needed in reporting and scheduling.</p>
 *
 * <p>This is a stateless utility class and cannot be instantiated.</p>
 *
 * @author Zhang Ze
 * @since 1.0.0
 */
public final class WeekUtils {

    private WeekUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the Monday of the week containing the given date.
     *
     * <p>Uses ISO week definition where Monday is the first day.</p>
     *
     * @param date the reference date, never {@code null}
     * @return the Monday of that week
     */
    public static LocalDate getMondayOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Returns the Sunday of the week containing the given date.
     *
     * <p>Uses ISO week definition where Sunday is the last day.</p>
     *
     * @param date the reference date, never {@code null}
     * @return the Sunday of that week
     */
    public static LocalDate getSundayOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Returns the Monday and Sunday of the week containing the given date.
     *
     * @param date the reference date, never {@code null}
     * @return a two-element array: {@code [Monday, Sunday]}
     */
    public static LocalDate[] getWeekRange(LocalDate date) {
        return new LocalDate[]{getMondayOfWeek(date), getSundayOfWeek(date)};
    }

    /**
     * Returns the Monday of the current week.
     *
     * @return this week's Monday
     */
    public static LocalDate getMondayOfCurrentWeek() {
        return getMondayOfWeek(LocalDate.now());
    }

    /**
     * Returns the Sunday of the current week.
     *
     * @return this week's Sunday
     */
    public static LocalDate getSundayOfCurrentWeek() {
        return getSundayOfWeek(LocalDate.now());
    }

    /**
     * Returns yesterday's date.
     *
     * @return yesterday
     */
    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    /**
     * Returns the date N days ago from today.
     *
     * @param days the number of days to go back
     * @return the date N days ago
     */
    public static LocalDate daysAgo(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Days must not be negative");
        }
        return LocalDate.now().minusDays(days);
    }

    /**
     * Returns the date N days from today (in the future).
     *
     * @param days the number of days to go forward
     * @return the date N days from now
     */
    public static LocalDate daysLater(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Days must not be negative");
        }
        return LocalDate.now().plusDays(days);
    }

    /**
     * Generates a list of dates for the last N days (including today),
     * ordered from most recent to oldest.
     *
     * @param count the number of days
     * @return a list of dates; empty if {@code count <= 0}
     */
    public static List<LocalDate> getRecentDaysAsDates(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<LocalDate> days = new ArrayList<>(count);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            days.add(today.minusDays(i));
        }
        return days;
    }

    /**
     * Returns the ISO week number of the given date.
     *
     * @param date the date
     * @return the week number (1–53)
     */
    public static int getWeekNumber(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /**
     * Returns the ISO week-based year of the given date.
     *
     * <p>The week-based year may differ from the calendar year for dates
     * near the start or end of December/January.</p>
     *
     * @param date the date
     * @return the week-based year
     */
    public static int getWeekBasedYear(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return date.get(IsoFields.WEEK_BASED_YEAR);
    }

    /**
     * Checks whether the given date is a weekend (Saturday or Sunday).
     *
     * @param date the date to check
     * @return {@code true} if the date is Saturday or Sunday
     */
    public static boolean isWeekend(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Checks whether the given date is a weekday (Monday through Friday).
     *
     * @param date the date to check
     * @return {@code true} if the date is Monday through Friday
     */
    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }
}
