# Architecture Guidelines

## Package Structure
```
com.localdeals.finder
├── data
│   ├── local         # Room databases, DAOs, DataStore preferences
│   ├── mock          # Fake in-memory repositories for v1
│   └── repository    # Repository implementations connecting to mock/local
├── di                # Hilt modules (AppModule, DatabaseModule, etc.)
├── domain
│   ├── model         # Pure Kotlin data classes (User, Deal, Seller, etc.)
│   ├── repository    # Repository interfaces defining data operations
│   └── usecase       # Business logic operations (e.g., AuthUseCases, AdminUseCases, DealUseCases)
├── ui
│   ├── components    # Reusable Jetpack Compose UI elements
│   ├── navigation    # Compose Navigation setup (NavGraphs, Routes)
│   ├── screens       # Compose Screens (Home, Login, Map, etc.)
│   └── theme         # Theme, Typography, Colors
└── util              # Helper extensions, constants
```

## Data Flow
- **Clean Architecture & MVVM**: UI layer (Compose) -> ViewModel -> UseCases (optional) -> Repository Interface (domain) -> Repository Implementation (data).
- **UiState Pattern**: ViewModels expose a single `uiState: StateFlow<UiState<T>>` to the UI, representing `Loading`, `Success`, `Empty`, or `Error`.
- **BaseViewModel**: A generic `BaseViewModel<T>` provides standard methods to emit state. Note: Some ViewModels (e.g. `LocationViewModel`) may extend `androidx.lifecycle.ViewModel` directly because Hilt can sometimes fail to resolve KSP bindings when inheriting from a generic `BaseViewModel` without providing a concrete type.
- **Navigation**: Controlled by `NavGraph.kt` using Navigation Compose. `LocalDealsApp.kt` holds the Scaffold and Bottom Navigation. Navigation actions happen within screens calling callbacks (e.g. `onNavigateNext`). Route protection is handled via `RoleGuardUseCase` and logic inside ViewModels, restricting Sellers and Admins to their respective dashboards.

## Key Decisions
- **Offline First & Room Local Database**: Replaced in-memory mock repositories with SQLite/Room in Step 13. All domain interfaces are bound to Room implementations in `AppModule.kt` and `DatabaseModule.kt`. Domain mappers (`EntityMappers.kt`) are cleanly decoupled from database entities.
- **Database Migrations Strategy**: `AppDatabase` is at version 1 with `exportSchema = true` (schemas stored in `app/schemas/`). Semantic versioning is enforced with explicit migration objects (`DatabaseMigrations.kt`). Destructive migrations are prohibited in production. A tested stub migration (`MIGRATION_1_2`) validates non-destructive schema evolution.
- **Bounding-Box & Haversine Distance**: High-performance local deal search uses a SQL bounding-box prefilter (`getDealsWithSellerInBoundingBox`) leveraging composite indices on `(latitude, longitude)`, followed by an exact Haversine distance sort in Kotlin.
- **Atomic Transactions**: Critical state flows are executed in atomic SQLite transactions:
  1. Order acceptance: updates order to `ACCEPTED`, decrements deal stock, and dispatches buyer notification.
  2. Review submission: inserts review and recalculates seller average rating.
  3. Deal deletion: automatically cascades to related stats, wishlist records, and reviews.
- **Security & Session Encryption**: Sensitive session authentication tokens stored in DataStore are encrypted using AES-GCM (`CryptoManager`) backed by the hardware-protected `AndroidKeyStore` (with graceful fallback for JVM test runners). Auto backup and data extraction rules specifically exclude databases and shared preferences (`android:allowBackup="false"`). All inputs are sanitized (`SanitizationUtil`) against XSS/control characters. Logs are masked against PII (emails, phone numbers, auth credentials) via `AppLogger`.
- **Navigation Role Guards**: Protected routes (`SellerDashboard`, `StoreProfileEdit`, `AddEditDeal`, `AdminPanel`) are defended with `AuthenticatedRoleGuard` which observes the authenticated user's role from `AuthRepository` and blocks unauthorized access.
- **Pure Local Analytics**: Analytics telemetry is completely self-contained via `AnalyticsTracker` backed by Room (`AnalyticsEventEntity`, `AnalyticsDao`, `RoomAnalyticsTracker`). Zero third-party SDK dependencies or telemetry costs. Feeds real-time event counts directly to Admin and Seller dashboards.
- **R8 Minification & Resource Shrinking**: Release builds enable full R8 code shrinking, dead code elimination, and resource shrinking (`isMinifyEnabled = true`, `isShrinkResources = true`). Comprehensive keep rules in `proguard-rules.pro` protect Room entities, DAOs, Hilt components, Coroutines, and WorkManager workers. Release APK compiles to an ultra-compact ~1.82 MB.
- **Zero Dependencies Cost**: Uses purely open-source libraries and no paid APIs (no Firebase, no Google Maps API keys).
- **Canvas Rendering Safety**: Direct usage of `drawText` on `Canvas` near edges causes buggy native calculations and crashes. A custom `DrawScope.safeDrawText` extension uses `TextMeasurer` manually and is mandated across all map and chart renderings.

## Dependency List & Versions
- Kotlin 1.9.23
- Compose BOM 2024.04.01
- Navigation Compose 2.7.7
- Hilt 2.51.1
- Room 2.6.1 (with room-ktx, room-compiler KSP, room-testing)
- DataStore 1.1.0
- WorkManager 2.9.0
- Robolectric 4.12.2 (for local JVM SQLite and DAO unit testing)
