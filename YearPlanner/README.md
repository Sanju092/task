# Year Planner - Android Task Planner

[![Build APK](https://github.com/Sanju092/task/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Sanju092/task/actions/workflows/build-apk.yml)

A personal Android task planner with home-screen widgets for daily, yearly overview, and yearly agenda views.

## ⬇️ Download APK

The latest debug APK is automatically built on every push via GitHub Actions.

1. Go to [**Actions tab**](https://github.com/Sanju092/task/actions/workflows/build-apk.yml) on GitHub
2. Click the latest successful workflow run
3. Scroll down to **Artifacts**
4. Download **`YearPlanner-debug-<run-number>`**

## Features

- **Year-at-a-glance**: View all 12 months simultaneously with task counts
- **Home screen widgets**: Today widget (4x2/4x3), Year Overview (4x4), Year Agenda (4x4+)
- **Flexible planning**: Day, month, year, or inbox precision for tasks
- **Recurrence**: Daily, weekly, monthly, yearly, and custom weekdays
- **Reminders**: AlarmManager-based notifications with exact/inexact modes
- **Offline-first**: Room database, no account required
- **Backup/Restore**: JSON and CSV import/export
- **Accessibility**: TalkBack support, large fonts, dark mode

## Architecture

```
app/
├── data/
│   ├── model/          # Room entities (Project, Task, TaskOccurrence, etc.)
│   ├── db/             # Room database, DAOs, TypeConverters
│   ├── repository/     # TaskRepository with Flow-based queries
│   └── seed/           # Sample data for first launch
├── domain/
│   ├── usecase/        # TaskUseCases - business logic
│   └── recurrence/     # RecurrenceGenerator - occurrence generation
├── reminders/          # AlarmManager scheduling, notifications
├── backup/             # JSON/CSV export/import
├── widget/             # Glance widgets (Today, YearOverview, YearAgenda)
├── ui/
│   ├── screens/        # Compose screens (Today, Year, Month, Projects, Settings)
│   ├── navigation/     # Navigation graph
│   └── theme/          # Material 3 theming
└── di/                 # Hilt modules
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Widgets**: Jetpack Glance
- **Database**: Room
- **DI**: Hilt
- **Async**: Coroutines + Flow
- **Background**: WorkManager
- **Reminders**: AlarmManager
- **Serialization**: kotlinx.serialization

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Project Structure (per developer plan)

This implementation follows the [Android Year Planner Developer Plan](Android_Year_Planner_Developer_Plan.md) with:
- Phase 0: Home-screen feasibility (widget + Room + completion)
- Phase 1: Task foundation (CRUD, projects, Today widget)
- Phase 2: Annual planning (12-month widget, year/month backlog)
- Phase 3: Recurrence & reminders
- Phase 4: Usability, backup, CSV import
- Phase 5: Device beta

## Widget Configuration

Each widget instance can be configured with:
- Selected year/month
- Project filter
- Show/hide completed
- Sort order
- Theme (light/dark/system)
- Privacy mode (hide sensitive titles)

## Data Model Highlights

- **TaskOccurrence** is the primary unit (not Task) - even non-recurring tasks have one occurrence
- **Stable occurrence keys** based on recurrence pattern, not date
- **Planning date ≠ Deadline** - separate concepts
- **Completion** is per-occurrence with undo support (24hr window)
- **Recurrence** generates on-demand up to 5 years ahead

## License

Personal project - see developer plan for scope and assumptions.