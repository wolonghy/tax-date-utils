# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-10-24

### Added

#### Core Date Utilities (TaxDateUtils)
- **15 built-in date format patterns** with cached `DateTimeFormatter` instances for thread-safe, high-performance formatting
  - LocalDate formats: `yyyy-MM-dd`, `yyyyMM`, `yyyy-MM`, `yyyyMMdd`, `yyyy/MM/dd`, `yyyy.MM.dd`, `yyMMdd`, `yyyy`
  - LocalDateTime formats: all date formats plus `yyyy-MM-dd HH:mm:ss`, `yyyyMMddHHmmss`, `yyyyMMddHHmmssSSS`, Chinese formats (`yyyy年MM月dd日 HH:mm:ss`, `yyyy年MM月dd日 HH时mm分ss秒`), `yyyy.MM.dd.HH:mm:ss`, `yyyy-MM-dd HH:mm`
- **Custom pattern formatting** via `format(LocalDate/LocalDateTime, String pattern)`
- **Date parsing** with `parseLocalDate()`, `parseMonth()`, `parseLocalDateTime()` supporting both default and custom patterns
- **Date validation** with `isValidDate()` and detailed `validateDate()` error messages
- **Date arithmetic**: `offsetYears()`, `offsetMonths()`, `offsetDays()`, `offset()` with `ChronoUnit`
- **Date difference calculation**: `daysBetween()`, `monthsBetween()`, `yearsBetween()`
- **Boundary calculations**: first/last day of month, quarter, half-year with leap year support
- **Date comparison**: `isBetween()`, `isOverlap()` for range checks
- **Component extraction**: `getYearStr()`, `getMonthStr()`, `getDayStr()`, `getHourStr()`, `getMinuteStr()`, `getSecondStr()` for both LocalDate and LocalDateTime
- **Type conversion**: epoch millis ↔ LocalDate, LocalDate ↔ LocalDateTime
- **Query utilities**: `now()`, `currentYear()`, `currentMonth()`, `getQuarter()`, `getMonthsBetween()`, `getRecentMonths()`, `getRecentDays()`, `getMonthsOfYear()`, `getQuartersOfYear()`, `getQuarterEndDates()`
- **Timestamp generation**: `nowTimestamp()`, `nowTimestampMs()`, `generateTimestampId()`

#### Tax Period Modeling (TaxPeriod)
- **Immutable record** representing tax periods: month, quarter, half-year, year, per-occurrence
- **Factory methods**: `ofMonth()`, `ofQuarter()`, `ofHalfYear()`, `ofYear()`, `ofOnce()`, `ofMonth(YearMonth)`
- **String parsing**: `parse()` auto-detects format (`202601`, `2026Q1`, `2026FH`, `2026SH`, `2026`, `202601N1`, `202601G1`)
- **Period navigation**: `previous()`, `next()`, `samePeriodLastYear()`
- **Date range conversion**: `toDateRange()` returns start and end dates
- **Correction period support**: `isCorrection()` for periods like `202601G1`

#### Period Resolution (TaxPeriodResolver)
- **Flag-based resolution**: `getTargetPeriod()` with 12 period flags (current, previous, same period last year, etc.)
- **Period type detection**: `detectPeriodType()` auto-identifies month/quarter/half-year/year from string
- **Declaration utilities**: `getDeclarationCycle()`, `getDeclarationMonth()`, `getCurrentPeriod()`
- **Validity-based inference**: `getPeriodTypeByValidity()` determines period type from date ranges
- **Navigation helpers**: `getPreviousQuarter()`, `getNextQuarter()`, `getPreviousMonth()`, `getNextMonth()`, `getMonthsInQuarter()`, `getMonthsInHalfYear()`, `getMonthsInYear()`
- **Period enumeration**: `getFullPeriodList()`, `assembleCompletePeriods()`

#### Period Arithmetic (TaxPeriodCalculator)
- **Offset operations**: `offsetMonths()`, `offsetQuarters()`, `offsetYears()`
- **Enumeration**: `enumerateMonths()`, `enumerateQuarters()` for listing periods in range
- **Distance calculation**: `monthsBetween()`, `quartersBetween()`

#### Deadline Calculation (TaxDeadlineCalculator)
- **Automatic deadline computation**: `calculateDeadline()` with default 15th day, weekend-adjusted
- **Custom deadline day**: `calculateDeadline(TaxPeriod, int)` for month-end deadlines
- **Weekend adjustment**: `adjustForWeekend()` shifts Saturday/Sunday to Monday
- **Holiday adjustment**: `adjustForWeekendAndHoliday()`, `adjustForChineseHoliday()` with holiday and makeup workday sets
- **Chinese holiday support**: `calculateDeadlineWithChineseHoliday()` for Spring Festival, National Day, etc.
- **Natural deadline query**: `getNaturalDeadlineDay()` returns 15 or 31 based on period type

#### Declaration Rule Engine (DeclareRuleEngine)
- **Forward calculation**: `generateReportPeriods()` maps declaration month to report periods
- **Reverse calculation**: `generateDeclareMonths()` maps report period to declaration months
- **Combined rules**: `processTaxPeriod()` handles common + special tax type rules
- **Builder-pattern configuration**: `DeclareRuleConfig` with `DeclareCycle` (monthly/quarterly/semi-annual/annual/per-occurrence) and `DeclareYearType` (current year/previous year)

#### Excel Date Utilities (ExcelDateUtils)
- **Excel numeric conversion**: `toLocalDate(double)` converts Excel serial dates to LocalDate
- **String parsing**: `toLocalDate(String)` handles Excel cell values
- **Formatting**: `format()`, `formatDate()`, `formatDateSlash()` for Excel numeric values
- **Validation**: `isExcelDate()` checks if numeric value is a valid Excel date
- **Reverse conversion**: `toExcelNumeric()` converts LocalDate to Excel serial number

#### OA Date Converter (OaDateConverter)
- **OA format conversion**: `toOaDate()` converts `yyyy/MM/dd` or LocalDate to `/OADate(n)/` format
- **Reverse conversion**: `fromOaDate()` converts `/OADate(n)/` to `yyyy-MM-dd` string or LocalDate
- **Integration support**: for OA system interfaces that use OLE Automation date format

#### Week Utilities (WeekUtils)
- **Week boundaries**: `getMondayOfWeek()`, `getSundayOfWeek()`, `getWeekRange()`
- **Current week**: `getMondayOfCurrentWeek()`, `getSundayOfCurrentWeek()`
- **Relative dates**: `yesterday()`, `daysAgo()`, `daysLater()`, `getRecentDaysAsDates()`
- **Week queries**: `getWeekNumber()` (ISO 8601), `getWeekBasedYear()`
- **Day type checks**: `isWeekend()`, `isWeekday()`

#### Domain Models
- **DateRange**: immutable record with `start()` and `end()`, plus `contains()`, `overlaps()`, `daysBetween()`, `monthsBetween()`, `formatStart()`, `formatEnd()`
- **DeclarationMonth**: record with `year()` and `month()`, plus `formattedMonth()`, `formattedMonthDash()`, `toYearMonth()`

#### Enums & Constants
- **PeriodType**: MONTH, QUARTER, HALF_YEAR, YEAR, ONCE
- **PeriodFlag**: 12 flags for relative period navigation (CURRENT, PREVIOUS, SAME_PERIOD_LAST_YEAR, etc.)
- **DeclareCycle**: declaration cycle frequencies with numeric codes
- **DeclareYearType**: T_C (current year), T_L (previous year)

#### Exception Handling
- **TaxDateException**: unchecked exception for invalid input or unsupported operations

### Changed

- N/A (initial release)

### Deprecated

- N/A (initial release)

### Removed

- N/A (initial release)

### Fixed

- N/A (initial release)

### Security

- N/A (initial release)

---

## Version History

- **1.0.0** (2026-10-24) — Initial release with comprehensive date utilities, tax period modeling, deadline calculation, and declaration rule engine
