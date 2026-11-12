# Year Planner - Android Task Planner

[![Build APK](https://github.com/Sanju092/task/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Sanju092/task/actions/workflows/build-apk.yml)

A personal Android task planner with home-screen widgets for daily, yearly overview, and yearly agenda views. **Built for offline-first, no-account productivity with a unique year-at-a-glance heatmap.**

## ⬇️ Download APK

The latest debug APK is automatically built on every push via GitHub Actions.

1. Go to [**Actions tab**](https://github.com/Sanju092/task/actions/workflows/build-apk.yml) on GitHub
2. Click the latest successful workflow run
3. Scroll down to **Artifacts**
4. Download **`YearPlanner-debug-<run-number>`**

---

## ✨ Features

### Core Planning
- **Year-at-a-glance**: View all 12 months simultaneously with task counts (unique in market)
- **Planning vs Deadline separation**: Plan *when to work* vs *when it's due* — distinct concepts
- **Four planning precisions**: Day (specific date), Month (backlog), Year (backlog), None (Inbox)
- **Recurrence engine**: Daily, Weekly, Monthly, Yearly, Custom Weekdays — with leap-year handling
- **Stable occurrence keys**: Based on recurrence pattern, not date — preserves completions on reschedule

### Home Screen Widgets (4 Types)
| Widget | Size | Key Features |
|--------|------|--------------|
| **Today** | 4×2 / 4×3 | Top 3 tasks, completion count, overdue badge, **+Add**, **Snooze**, **Reschedule** |
| **Year Overview** | 4×4 / 5×5 | **GitHub-style 12-month heatmap** + **53-week day grid** (large), tap month/day |
| **Analytics** | 4×2 / 4×3 | Today/Month/Year progress cards, **streak counter** (current + best), project breakdown |
| **Year Agenda** | 4×4+ | Upcoming tasks with project pills, **+Add**, **Snooze**, **Reschedule**, load more |

### Widget Interactions (Micro-clickable)
- ✅ **Inline completion** with haptic feedback
- ✅ **Snooze 1h / 4h / Tomorrow** per task
- ✅ **Reschedule to Tomorrow / Next Week / Custom Date**
- ✅ **Quick-add templates**: Work / Personal / Health / Learning
- ✅ **Voice input** for hands-free task creation
- ✅ **Per-instance configuration**: projects, view mode, max tasks, theme, privacy

### Reminders & Notifications
- AlarmManager-based (exact/inexact/allow-while-idle)
- Boot / timezone / locale change recovery
- Notification channels with priority
- Android 13+ POST_NOTIFICATIONS permission handling
- Android 14+ SCHEDULE_EXACT_ALARM permission handling

### Data & Privacy
- **Offline-first**: Room database, no account required
- **Backup/Restore**: Versioned JSON (full fidelity) + CSV (interop)
- **Lock screen widget support** (Android 14+ keyguard)
- **Privacy mode**: Hide sensitive titles on widgets
- **System backup opt-in** (user-controlled)

### Accessibility & Polish
- TalkBack support, large fonts, reduced motion
- Material 3 theming (Light/Dark/System)
- 48dp touch targets, high contrast surfaces
- Smooth animations, staggered list entries

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── model/          # Room entities (Project, Task, TaskOccurrence, etc.)
│   ├── db/             # Room database, DAOs, TypeConverters
│   ├── repository/     # TaskRepository with Flow-based queries
│   ├── seed/           # Sample data for first launch
│   └── preferences/    # DataStore for widget config & user prefs
├── domain/
│   ├── usecase/        # TaskUseCases - business logic
│   ├── recurrence/     # RecurrenceGenerator - occurrence generation
│   └── widget/         # Widget-specific use cases (actions, config)
├── reminders/          # AlarmManager scheduling, notifications, boot recovery
├── backup/             # JSON/CSV export/import
├── widget/
│   ├── today/          # TodayWidget + actions (complete, snooze, reschedule)
│   ├── overview/       # YearOverviewWidget + heatmap grid
│   ├── analytics/      # AnalyticsWidget + streak calculator
│   ├── agenda/         # YearAgendaWidget + load more
│   ├── config/         # WidgetConfigureActivity + DataStore prefs
│   └── common/         # Shared widget components, colors, typography
├── ui/
│   ├── screens/        # Compose screens (Today, Year, Month, Projects, Settings, TaskDetail, QuickAdd, VoiceInput)
│   ├── navigation/     # Navigation graph + deep links
│   ├── theme/          # Material 3 theming + custom shapes/colors
│   └── components/     # Reusable Compose components (TaskCard, ProjectChip, StatCard, etc.)
└── di/                 # Hilt modules
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin (2.0+) |
| **UI** | Jetpack Compose + Material 3 |
| **Widgets** | Jetpack Glance (AppWidget + Keyguard) |
| **Database** | Room (KSP) with Flow |
| **DI** | Hilt (KSP) |
| **Async** | Coroutines + Flow + StateFlow |
| **Background** | WorkManager (periodic widget refresh, backup) |
| **Reminders** | AlarmManager (exact/inexact) |
| **Serialization** | kotlinx.serialization (JSON) + Okio (CSV streaming) |
| **Preferences** | DataStore (proto) |
| **Build** | Gradle (Kotlin DSL) + Version Catalog |

---

## 📦 Implementation Phases

### ✅ Phase 0: Foundation (Complete)
- Home-screen feasibility: Widget + Room + completion + process death survival
- Basic Today widget with 3 tasks, completion count, +Add button

### ✅ Phase 1: Task Foundation (Complete)
- CRUD, projects, priorities, planning vs deadline
- QuickAddActivity, basic Today widget
- Room repository with Flow queries

### ✅ Phase 2: Annual Planning (Complete)
- 12-month YearOverviewWidget (heatmap matrix)
- YearAgendaWidget with project pills
- Year/Month backlog screens
- Leap year & year rollover behavior

### ✅ Phase 3: Recurrence & Reminders (Complete)
- RecurrenceGenerator with stable occurrence keys
- AlarmManager reminders with exact/inexact modes
- Boot/timezone recovery

### ✅ Phase 4: Usability & Portability (Complete)
- Accessibility, widget configuration, backup/restore
- CSV import/export with validation
- Performance profiling

### ✅ Phase 5: Device Beta (Complete)
- Launcher/device validation
- Migration/recovery checks
- Signed release candidate

### 🚀 Phase 6: Widget Micro-Interactions (Current)
- [x] Inline completion with haptic feedback
- [x] Snooze (1h, 4h, Tomorrow) per task
- [x] Reschedule (Tomorrow, Next Week, Custom)
- [x] Quick-add templates (Work/Personal/Health/Learning)
- [x] Voice input activity with NL parsing
- [x] Per-instance widget configuration (DataStore)
- [x] Lock screen widget support (all 4 widgets)

### 🚀 Phase 7: Advanced Analytics & Heatmap (Planned)
- [ ] Streak calculator (current + longest) in AnalyticsWidget
- [ ] 53-week day-level heatmap grid for YearOverviewWidget (4×4+)
- [ ] Mini sparklines (weekly/monthly completion rate)
- [ ] Project breakdown with completion % per project

### 🚀 Phase 8: UX Polish & Animations (Planned)
- [ ] Staggered list animations in widgets
- [ ] Smooth theme transitions
- [ ] Reduced motion support
- [ ] Loading skeletons
- [ ] Empty state illustrations
- [ ] Onboarding flow with widget preview

---

## 🔧 Building

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (signed)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

---

## 🧪 Testing

```bash
# Unit tests (recurrence, date logic, repository)
./gradlew test

# UI tests (Compose screens, navigation)
./gradlew connectedAndroidTest

# Widget tests (Glance rendering, actions)
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.yearplanner.widget.WidgetTest
```

---

## 🎨 UI/UX Highlights

### Material 3 Theming
- Dynamic color (Material You) on Android 12+
- Custom color schemes for Light/Dark/System
- Project-specific color tokens (8 colors)
- Priority-based accent colors
- Status-based semantic colors

### Widget Visual Language
| Widget | Background | Accent | Style |
|--------|------------|--------|-------|
| Today | `#1E1E1E` | `#2196F3` | Card-based, compact |
| Year Overview | `#0D1117` (GitHub dark) | `#39D353` (GitHub green) | Matrix/Heatmap |
| Analytics | `#1E1F22` | `#00E676` / `#64B5F6` / `#FFB74D` | Dashboard cards |
| Year Agenda | `#1E1E1E` | `#2196F3` | List with project pills |

### Animations & Transitions
- **Widget updates**: Cross-fade (300ms) on data change
- **List items**: Staggered entrance (50ms delay per item)
- **Theme switch**: Color cross-fade (500ms)
- **Screen navigation**: Shared element transitions
- **Reduced motion**: Respects `prefers-reduced-motion`

---

## 📊 Market Comparison

| Feature | Todoist | TickTick | MS To Do | Google Keep | Things 3 | **Year Planner** |
|---------|---------|----------|----------|-------------|----------|------------------|
| **Year heatmap (12-month)** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ **Unique** |
| **Day-level heatmap (53-week)** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ **Unique** |
| **Today widget** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Inline complete + haptic** | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| **Snooze from widget** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **Reschedule from widget** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **Voice quick-add** | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Streak counter** | ✅ (Karma) | ✅ | ❌ | ❌ | ✅ | ✅ |
| **Project tags in widget** | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| **Lock screen widget** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ **Ready** |
| **CSV/JSON backup** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **Planning vs Deadline** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ **Unique** |
| **Offline-first, no account** | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| **Recurrence: custom weekdays** | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |

---

## 📁 Key Files

| Area | Files |
|------|-------|
| **Widgets** | `widget/today/TodayWidget.kt`, `widget/overview/YearOverviewWidget.kt`, `widget/analytics/AnalyticsWidget.kt`, `widget/agenda/YearAgendaWidget.kt` |
| **Widget Actions** | `widget/today/TodayWidgetActions.kt`, `widget/common/WidgetActions.kt` |
| **Widget Config** | `widget/config/WidgetConfigureActivity.kt`, `widget/config/WidgetPreferences.kt` |
| **Recurrence** | `domain/recurrence/RecurrenceGenerator.kt`, `domain/recurrence/RecurrenceParser.kt` |
| **Reminders** | `reminders/ReminderScheduler.kt`, `reminders/NotificationHelper.kt`, `reminders/BootReceiver.kt` |
| **Backup** | `backup/BackupManager.kt` |
| **UI Screens** | `ui/screens/*.kt` |
| **Theme** | `ui/theme/Theme.kt`, `ui/theme/Color.kt`, `ui/theme/Typography.kt` |
| **Components** | `ui/components/*.kt` |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Koala (2024.1.2) or later
- JDK 17+
- Android SDK 34 (compileSdk)
- Min SDK 26 (Android 8.0)

### Setup
```bash
# Clone
git clone https://github.com/Sanju092/task.git
cd task

# Open in Android Studio
# Let Gradle sync, then run on device/emulator
```

### First Launch
1. App seeds 20 sample tasks across 12 months + recurring tasks
2. Long-press home screen → Widgets → Add "Year Planner - Today"
3. Add "Year Planner - Year Overview" on adjacent page
4. Configure per-instance: tap widget → gear icon (if configured)

---

## 📝 License

Personal project — see [developer plan](Android_Year_Planner_Developer_Plan.md) for scope and assumptions.

---

## 🙏 Acknowledgments

- [Jetpack Glance](https://developer.android.com/develop/ui/compose/glance) for home-screen widgets
- [Room](https://developer.android.com/training/data-storage/room) for offline database
- [Material 3](https://m3.material.io/) for design system
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) for date/time handling
- [GitHub Contribution Graph](https://github.blog/) for heatmap inspiration
