# tax-date-utils

[![Maven Central](https://img.shields.io/maven-central/v/com.wolonghy/tax-date-utils.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.wolonghy/tax-date-utils)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java%2017%2B-orange.svg)](https://adoptium.net/)

A lightweight Java library for **tax date and period handling** in financial and tax systems — built entirely on `java.time` (zero `java.util.Date` / `Calendar` usage). Covers date formatting, parsing, calculation, tax period modeling, deadline computation, and declaration rule engine.

## Features

- **100% `java.time`** — all APIs use `LocalDate`, `LocalDateTime`, `YearMonth`; no legacy date types
- **15 built-in date formats** — Chinese, dot-separated, slash, compact, short-year, and more
- **Tax period modeling** — month, quarter, half-year, year, and per-occurrence periods with navigation
- **Deadline calculation** — automatic weekend/holiday adjustment, Chinese public holiday support
- **Declaration rule engine** — forward (declare month → report period) and reverse (report period → declare months)
- **Period flag resolution** — 12 flag codes (current, previous, same period last year, etc.)
- **Excel date conversion** — Excel numeric dates ↔ `LocalDate`
- **OA date conversion** — `/OADate(n)/` format ↔ `LocalDate`
- **Week utilities** — week boundaries, week numbers, weekend/weekday checks
- **Zero external dependencies** — JDK 17+ only
- **Immutable records** — all models are Java `record` types, thread-safe by design

## Installation

### Maven

```xml
<dependency>
    <groupId>com.wolonghy</groupId>
    <artifactId>tax-date-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.wolonghy:tax-date-utils:1.0.0'
```

## Quick Start

### Date Formatting & Parsing

```java
import com.wolonghy.taxdate.util.TaxDateUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Format LocalDate
LocalDate date = LocalDate.of(2026, 1, 15);
TaxDateUtils.formatDate(date);          // "2026-01-15"
TaxDateUtils.formatDateCompact(date);   // "20260115"
TaxDateUtils.formatDateSlash(date);     // "2026/01/15"
TaxDateUtils.formatMonth(date);         // "202601"

// Format LocalDateTime
LocalDateTime dt = LocalDateTime.of(2026, 1, 15, 14, 30, 45);
TaxDateUtils.formatDateTime(dt);        // "2026-01-15 14:30:45"
TaxDateUtils.formatDateTimeCn(dt);      // "2026年01月15日 14:30:45"
TaxDateUtils.formatDateTimeCnFull(dt);  // "2026年01月15日 14时30分45秒"

// Custom pattern
TaxDateUtils.format(date, "yyyy/MM/dd");

// Parse
TaxDateUtils.parseLocalDate("2026-01-15");
TaxDateUtils.parseMonth("202601");
```

### Tax Period Operations

```java
import com.wolonghy.taxdate.model.TaxPeriod;

// Create periods
TaxPeriod month = TaxPeriod.ofMonth(2026, 1);        // "202601"
TaxPeriod quarter = TaxPeriod.ofQuarter(2026, 2);     // "2026Q2"
TaxPeriod halfYear = TaxPeriod.ofHalfYear(2026, true); // "2026FH"
TaxPeriod year = TaxPeriod.ofYear(2026);              // "2026"

// Parse from string
TaxPeriod.parse("202601");    // monthly
TaxPeriod.parse("2026Q1");    // quarterly
TaxPeriod.parse("2026FH");    // first half
TaxPeriod.parse("2026");      // annual

// Navigate
month.next();                  // → 202602
month.previous();              // → 202512
month.samePeriodLastYear();    // → 202501

// Convert to date range
month.toDateRange();           // DateRange[2026-01-01, 2026-01-31]
```

### Deadline Calculation

```java
import com.wolonghy.taxdate.deadline.TaxDeadlineCalculator;
import java.time.LocalDate;

TaxPeriod period = TaxPeriod.ofMonth(2026, 1);

// Default deadline (15th), auto-adjusted for weekends
LocalDate deadline = TaxDeadlineCalculator.calculateDeadline(period);

// With Chinese holidays and makeup workdays
Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 1, 26));
Set<LocalDate> makeupWorkdays = Set.of(LocalDate.of(2026, 1, 24));
LocalDate adjusted = TaxDeadlineCalculator.calculateDeadlineWithChineseHoliday(
    period, 15, holidays, makeupWorkdays);
```

### Declaration Rule Engine

```java
import com.wolonghy.taxdate.declare.*;

// Configure VAT (monthly declaration)
DeclareRuleConfig vatConfig = DeclareRuleConfig.defaultMonthly();

// Configure CIT (quarterly declaration)
DeclareRuleConfig citConfig = DeclareRuleConfig.defaultQuarterly();

// Forward: declare month → report periods
List<DeclareRulePeriodWrapper> periods =
    DeclareRuleEngine.generateReportPeriods(vatConfig, "202601");

// Reverse: report period → declare months
List<String> declareMonths =
    DeclareRuleEngine.generateDeclareMonths(citConfig, "2026Q1");
```

## Supported Formats & Scenarios

### A. Date Formatting

#### LocalDate Formats

| Method | Output | Pattern |
|--------|--------|---------|
| `formatDate(LocalDate)` | `2026-01-15` | `yyyy-MM-dd` |
| `formatMonth(LocalDate)` | `202601` | `yyyyMM` |
| `formatMonthDash(LocalDate)` | `2026-01` | `yyyy-MM` |
| `formatDateCompact(LocalDate)` | `20260115` | `yyyyMMdd` |
| `formatDateSlash(LocalDate)` | `2026/01/15` | `yyyy/MM/dd` |
| `formatDateDot(LocalDate)` | `2026.01.15` | `yyyy.MM.dd` |
| `formatDateShort(LocalDate)` | `260115` | `yyMMdd` |
| `formatYear(LocalDate)` | `2026` | `yyyy` |

#### LocalDateTime Formats

| Method | Output | Pattern |
|--------|--------|---------|
| `formatDate(LocalDateTime)` | `2026-01-15` | `yyyy-MM-dd` |
| `formatMonth(LocalDateTime)` | `202601` | `yyyyMM` |
| `formatMonthDash(LocalDateTime)` | `2026-01` | `yyyy-MM` |
| `formatDateCompact(LocalDateTime)` | `20260115` | `yyyyMMdd` |
| `formatDateSlash(LocalDateTime)` | `2026/01/15` | `yyyy/MM/dd` |
| `formatDateDot(LocalDateTime)` | `2026.01.15` | `yyyy.MM.dd` |
| `formatDateShort(LocalDateTime)` | `260115` | `yyMMdd` |
| `formatYear(LocalDateTime)` | `2026` | `yyyy` |
| `formatDateTime(LocalDateTime)` | `2026-01-15 14:30:45` | `yyyy-MM-dd HH:mm:ss` |
| `formatDateTimeCompact(LocalDateTime)` | `20260115143045` | `yyyyMMddHHmmss` |
| `formatDateTimeMs(LocalDateTime)` | `20260115143045123` | `yyyyMMddHHmmssSSS` |
| `formatDateTimeCn(LocalDateTime)` | `2026年01月15日 14:30:45` | `yyyy年MM月dd日 HH:mm:ss` |
| `formatDateTimeCnFull(LocalDateTime)` | `2026年01月15日 14时30分45秒` | `yyyy年MM月dd日 HH时mm分ss秒` |
| `formatDateTimeDot(LocalDateTime)` | `2026.01.15.14:30:45` | `yyyy.MM.dd.HH:mm:ss` |
| `formatDateTimeMinute(LocalDateTime)` | `2026-01-15 14:30` | `yyyy-MM-dd HH:mm` |

#### Custom Format

| Method | Description |
|--------|-------------|
| `format(LocalDate, String pattern)` | Format with any `DateTimeFormatter` pattern |
| `format(LocalDateTime, String pattern)` | Format with any `DateTimeFormatter` pattern |

### B. Date Parsing

| Method | Description | Example |
|--------|-------------|---------|
| `parseLocalDate(String)` | Parse `yyyy-MM-dd` | `"2026-01-15"` → `LocalDate` |
| `parseLocalDate(String, String pattern)` | Parse with custom pattern | `"2026/01/15"` + `"yyyy/MM/dd"` |
| `parseMonth(String)` | Parse `yyyyMM` to first day of month | `"202601"` → `2026-01-01` |
| `parseLocalDateTime(String, String pattern)` | Parse datetime with pattern | `"2026-01-15 14:30:00"` |
| `isValidDate(String)` | Validate `yyyy-MM-dd` format | Includes leap year check |
| `isValidDate(String, String pattern)` | Validate with custom pattern | |
| `validateDate(String, String pattern)` | Return detailed error message (null = valid) | |

### C. Date Calculation

| Method | Description | Example |
|--------|-------------|---------|
| `offsetYears(LocalDate, int)` | Add/subtract years | `offsetYears(2026-06-15, 2)` → `2028-06-15` |
| `offsetMonths(LocalDate, int)` | Add/subtract months | `offsetMonths(2026-01-15, 3)` → `2026-04-15` |
| `offsetDays(LocalDate, int)` | Add/subtract days | `offsetDays(2026-01-15, 10)` → `2026-01-25` |
| `offset(LocalDate, ChronoUnit, long)` | Generic offset (any temporal unit) | |
| `daysBetween(LocalDate, LocalDate)` | Days between two dates | |
| `daysBetween(String, String)` | Days between string dates | |
| `monthsBetween(LocalDate, LocalDate)` | Months between two dates | |
| `yearsBetween(LocalDate, LocalDate)` | Years between two dates | |
| `getFirstDayOfMonth(int, int)` | First day of month | `getFirstDayOfMonth(2026, 3)` → `2026-03-01` |
| `getLastDayOfMonth(int, int)` | Last day of month (leap-year aware) | `getLastDayOfMonth(2024, 2)` → `2024-02-29` |
| `getLastDayOfMonthStr(int, int)` | Last day as `yyyy-MM-dd` string | |
| `getLastDayOfMonthStr(int, int, String)` | Last day with custom pattern | |
| `getFirstDayOfQuarter(int, int)` | First day of quarter | `getFirstDayOfQuarter(2026, 2)` → `2026-04-01` |
| `getLastDayOfQuarter(int, int)` | Last day of quarter | `getLastDayOfQuarter(2026, 2)` → `2026-06-30` |
| `getFirstDayOfFirstHalf(int)` | First day of first half | → `yyyy-01-01` |
| `getLastDayOfFirstHalf(int)` | Last day of first half | → `yyyy-06-30` |
| `getFirstDayOfSecondHalf(int)` | First day of second half | → `yyyy-07-01` |
| `getLastDayOfSecondHalf(int)` | Last day of second half | → `yyyy-12-31` |

### D. Date Comparison & Judgment

| Method | Description |
|--------|-------------|
| `isLeapYear(int)` | Check if year is a leap year |
| `isBetween(LocalDate, LocalDate, LocalDate)` | Check if date is within range (inclusive) |
| `isOverlap(LocalDate, LocalDate, LocalDate, LocalDate)` | Check if two date ranges overlap |
| `isOverlap(String, String, String, String)` | Check overlap with string dates |
| `isWeekend(LocalDate)` | Check if weekend (`WeekUtils`) |
| `isWeekday(LocalDate)` | Check if weekday (`WeekUtils`) |

### E. Type Conversion

| Method | Description |
|--------|-------------|
| `toLocalDate(long epochMilli)` | Epoch millis → `LocalDate` |
| `toLocalDateTime(LocalDate)` | `LocalDate` → `LocalDateTime` (at start of day) |
| `toLocalDateTime(LocalDate, int, int, int)` | `LocalDate` → `LocalDateTime` (with specific time) |
| `toLocalDate(LocalDateTime)` | `LocalDateTime` → `LocalDate` (extract date part) |
| `now()` | Current date |
| `now(ZoneId)` | Current date in specified time zone |
| `nowDateTime(ZoneId)` | Current datetime in specified time zone |
| `currentYear()` | Current year |
| `currentMonth()` | Current month |
| `nowTimestamp()` | Current timestamp as `yyyyMMddHHmmss` |
| `nowTimestampMs()` | Current millisecond timestamp as `yyyyMMddHHmmssSSS` |
| `generateTimestampId(int)` | Generate unique timestamp-based ID |

### F. Component Extraction

| Method | Applicable Type | Output Example |
|--------|----------------|----------------|
| `getYearStr()` | LocalDate / LocalDateTime | `"2026"` |
| `getMonthStr()` | LocalDate / LocalDateTime | `"01"` |
| `getDayStr()` | LocalDate / LocalDateTime | `"15"` |
| `getHourStr()` | LocalDateTime | `"14"` |
| `getMinuteStr()` | LocalDateTime | `"30"` |
| `getSecondStr()` | LocalDateTime | `"45"` |

### G. Tax Period Business

#### G1. Period Modeling (TaxPeriod)

| Operation | Method | Example |
|-----------|--------|---------|
| Create monthly | `TaxPeriod.ofMonth(year, month)` | `202601` |
| Create quarterly | `TaxPeriod.ofQuarter(year, quarter)` | `2026Q2` |
| Create first half | `TaxPeriod.ofHalfYear(year, true)` | `2026FH` |
| Create second half | `TaxPeriod.ofHalfYear(year, false)` | `2026SH` |
| Create yearly | `TaxPeriod.ofYear(year)` | `2026` |
| Create per-occurrence | `TaxPeriod.ofOnce(year, month)` | `202601N1` |
| Create from YearMonth | `TaxPeriod.ofMonth(YearMonth)` | |
| Parse string | `TaxPeriod.parse(String)` | Auto-detect format |
| Previous period | `period.previous()` | `202601` → `202512` |
| Next period | `period.next()` | `2026Q1` → `2026Q2` |
| Same period last year | `period.samePeriodLastYear()` | `202601` → `202501` |
| Convert to date range | `period.toDateRange()` | `202601` → `[2026-01-01, 2026-01-31]` |
| Convert to YearMonth | `period.toYearMonth()` | |
| Check if correction | `period.isCorrection()` | `202601G1` → `true` |

#### G2. Period Arithmetic (TaxPeriodCalculator)

| Method | Description |
|--------|-------------|
| `offsetMonths(TaxPeriod, int)` | Offset by N months |
| `offsetQuarters(TaxPeriod, int)` | Offset by N quarters |
| `offsetYears(TaxPeriod, int)` | Offset by N years |
| `enumerateMonths(from, to)` | Enumerate all months in range |
| `enumerateQuarters(from, to)` | Enumerate all quarters in range |
| `monthsBetween(from, to)` | Count months between periods |
| `quartersBetween(from, to)` | Count quarters between periods |

#### G3. Period Resolution (TaxPeriodResolver)

| Method | Description |
|--------|-------------|
| `getTargetPeriod(period, flag)` | Resolve target period by flag code |
| `detectPeriodType(String)` | Auto-detect period type |
| `getDeclarationCycle(String)` | Get declaration cycle type |
| `getDeclarationMonth(String)` | Calculate declaration month |
| `getCurrentPeriod()` | Get current tax period |
| `getPeriodByFlag(int flag)` | Get period by flag code |
| `getPeriodTypeByValidity(start, end)` | Infer period type from validity dates |
| `getQuarterFromMonth(String)` | Month → quarter |
| `getMonthsInQuarter(String)` | Quarter → month list |
| `getMonthsInHalfYear(String)` | Half-year → month list |
| `getPreviousQuarter` / `getNextQuarter` | Quarter navigation |
| `getPreviousMonth` / `getNextMonth` | Month navigation |
| `getLastQuarter(String)` | Get last quarter |
| `getFullPeriodList(start, end)` | Get complete period list in range |
| `assembleCompletePeriods(List)` | Assemble complete periods |

#### G4. Period Flags (PeriodFlag)

| Flag | Meaning | Example (base: 202601) |
|------|---------|------------------------|
| `0` | Current period | `202601` |
| `1` | Previous period | `202512` |
| `2` | Same period last year | `202501` |
| `3` | Previous full year | `2025` |
| `4` | Previous quarter | `2025Q4` |
| `5` | First half | `2026FH` |
| `6` | Second half | `2026SH` |
| `7` | Current (per-occurrence) | `202601N1` |
| `8` | Previous (per-occurrence) | |
| `9` | Same period last year (per-occurrence) | |
| `10` | Correction current | `202601G1` |
| `11` | Correction previous | |

#### G5. Deadline Calculation (TaxDeadlineCalculator)

| Method | Description |
|--------|-------------|
| `calculateDeadline(TaxPeriod)` | Default 15th deadline, weekend-adjusted |
| `calculateDeadline(TaxPeriod, int)` | Custom deadline day |
| `adjustForWeekend(LocalDate)` | Shift weekend to Monday |
| `adjustForWeekendAndHoliday(LocalDate, Set)` | Shift weekends and holidays |
| `adjustForChineseHoliday(LocalDate, Set, Set)` | Chinese holidays + makeup workdays |
| `calculateDeadlineWithChineseHoliday(...)` | Full holiday-adjusted deadline |
| `getNaturalDeadlineDay(TaxPeriod)` | Get natural deadline day (15 or 31) |

#### G6. Declaration Rule Engine (DeclareRuleEngine)

| Method | Description |
|--------|-------------|
| `generateReportPeriods(config, declareMonth)` | Forward: declare month → report period list |
| `generateDeclareMonths(config, reportPeriod)` | Reverse: report period → declare month list |
| `processTaxPeriod(commonConfig, specialConfig, declareMonth)` | Combined common + special rules |
| `generateReportPeriodsByRule(config, declareMonth)` | Generate by single rule |

#### G7. Month/Quarter Enumeration (TaxDateUtils)

| Method | Description |
|--------|-------------|
| `getMonthsBetween(LocalDate, LocalDate)` | All months in range (`yyyyMM`) |
| `getMonthsBetween(String, String)` | All months (string input) |
| `getMonthsBetweenDash(LocalDate, LocalDate)` | All months (`yyyy-MM`) |
| `getRecentMonths(int)` | Last N months |
| `getRecentDays(int)` | Last N days |
| `getRecentDays(int, String)` | Last N days (custom format) |
| `getMonthsOfYear(int)` | All 12 months of year |
| `getQuartersOfYear(int)` | All 4 quarters of year |
| `getQuarterEndDates(String, String)` | Quarter end dates in range |
| `getQuarter(int month)` | Month → quarter number |

### H. Excel / OA Integration

#### H1. Excel Dates (ExcelDateUtils)

| Method | Description |
|--------|-------------|
| `toLocalDate(double)` | Excel numeric → `LocalDate` |
| `toLocalDate(String)` | Excel cell string → `LocalDate` |
| `format(double, String)` | Excel numeric → custom format string |
| `formatDate(double)` | Excel numeric → `yyyy-MM-dd` |
| `formatDateSlash(double)` | Excel numeric → `yyyy/MM/dd` |
| `isExcelDate(double)` | Check if valid Excel date number |
| `toExcelNumeric(LocalDate)` | `LocalDate` → Excel numeric |

#### H2. OA Dates (OaDateConverter)

| Method | Description |
|--------|-------------|
| `toOaDate(String)` | `yyyy/MM/dd` → `/OADate(n)/` |
| `toOaDate(LocalDate)` | `LocalDate` → `/OADate(n)/` |
| `fromOaDate(String)` | `/OADate(n)/` → `yyyy-MM-dd` |
| `fromOaDateToDate(String)` | `/OADate(n)/` → `LocalDate` |

### I. Week & Workday (WeekUtils)

| Method | Description |
|--------|-------------|
| `getMondayOfWeek(LocalDate)` | Get Monday of the week |
| `getSundayOfWeek(LocalDate)` | Get Sunday of the week |
| `getWeekRange(LocalDate)` | Get [Monday, Sunday] of the week |
| `getMondayOfCurrentWeek()` | This Monday |
| `getSundayOfCurrentWeek()` | This Sunday |
| `yesterday()` | Yesterday |
| `daysAgo(int)` | N days ago |
| `daysLater(int)` | N days later |
| `getRecentDaysAsDates(int)` | Last N days as `List<LocalDate>` |
| `getWeekNumber(LocalDate)` | Get week number (ISO 8601) |
| `getWeekBasedYear(LocalDate)` | Get week-based year |
| `isWeekend(LocalDate)` | Check if weekend |
| `isWeekday(LocalDate)` | Check if weekday |

## API Reference

### Pattern Constants

All pattern constants are defined in `TaxDateUtils`:

| Constant | Value | Usage |
|----------|-------|-------|
| `PATTERN_DATE` | `yyyy-MM-dd` | ISO date |
| `PATTERN_DATE_COMPACT` | `yyyyMMdd` | Compact date |
| `PATTERN_DATE_SLASH` | `yyyy/MM/dd` | Slash date |
| `PATTERN_DATE_DOT` | `yyyy.MM.dd` | Dot date |
| `PATTERN_DATE_SHORT` | `yyMMdd` | Short year date |
| `PATTERN_MONTH` | `yyyyMM` | Month |
| `PATTERN_MONTH_DASH` | `yyyy-MM` | Month with dash |
| `PATTERN_YEAR` | `yyyy` | Year |
| `PATTERN_DATETIME` | `yyyy-MM-dd HH:mm:ss` | Datetime |
| `PATTERN_DATETIME_COMPACT` | `yyyyMMddHHmmss` | Compact datetime |
| `PATTERN_DATETIME_MS` | `yyyyMMddHHmmssSSS` | Millisecond timestamp |
| `PATTERN_DATETIME_CN` | `yyyy年MM月dd日 HH:mm:ss` | Chinese datetime |
| `PATTERN_DATETIME_CN_FULL` | `yyyy年MM月dd日 HH时mm分ss秒` | Chinese full datetime |
| `PATTERN_DATETIME_DOT` | `yyyy.MM.dd.HH:mm:ss` | Dot datetime |
| `PATTERN_DATETIME_MINUTE` | `yyyy-MM-dd HH:mm` | Datetime to minute |

## Requirements

- Java 17 or later

## Documentation

- **Javadoc**: [https://javadoc.io/doc/com.wolonghy/tax-date-utils](https://javadoc.io/doc/com.wolonghy/tax-date-utils)
- **API Reference**: See the [Supported Formats & Scenarios](#supported-formats--scenarios) section above for a comprehensive list of all available methods

## Build from Source

```bash
git clone https://github.com/wolonghy/tax-date-utils.git
cd tax-date-utils
mvn clean install
```

To skip tests:

```bash
mvn clean install -DskipTests
```

## Project Links

- **Source Code**: [https://github.com/wolonghy/tax-date-utils](https://github.com/wolonghy/tax-date-utils)
- **Issue Tracker**: [https://github.com/wolonghy/tax-date-utils/issues](https://github.com/wolonghy/tax-date-utils/issues)
- **Maven Central**: [https://central.sonatype.com/artifact/com.wolonghy/tax-date-utils](https://central.sonatype.com/artifact/com.wolonghy/tax-date-utils)
- **Javadoc**: [https://javadoc.io/doc/com.wolonghy/tax-date-utils](https://javadoc.io/doc/com.wolonghy/tax-date-utils)

## Contributing

Contributions are welcome! Here's how you can help:

1. **Report bugs**: Open an issue at [GitHub Issues](https://github.com/wolonghy/tax-date-utils/issues)
2. **Suggest features**: Open a feature request issue
3. **Submit pull requests**: 
   - Fork the repository
   - Create a feature branch (`git checkout -b feature/your-feature`)
   - Write code and tests
   - Ensure all tests pass (`mvn test`)
   - Submit a pull request

### Development Guidelines

- All date APIs must be based on `java.time` — **no** `java.util.Date`, `java.util.Calendar`, or related legacy types
- New public methods must include complete Javadoc (`@param`, `@return`, `@throws`)
- New features must include corresponding unit tests
- Follow existing code style: utility classes are `final` with private constructors, models prefer `record` types

## Reporting Issues

Found a bug or have a question? Please [open an issue](https://github.com/wolonghy/tax-date-utils/issues) on GitHub.

When reporting bugs, please include:
- The version of tax-date-utils you're using
- Your Java version (`java -version`)
- A minimal code example that reproduces the issue
- Expected vs actual behavior

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
