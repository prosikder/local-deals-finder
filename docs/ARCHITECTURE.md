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
│   └── usecase       # Business logic operations
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
- **BaseViewModel**: A generic `BaseViewModel<T>` provides standard methods to emit state.
- **Navigation**: Controlled by `NavGraph.kt` using Navigation Compose. `LocalDealsApp.kt` holds the Scaffold and Bottom Navigation. Navigation actions happen within screens calling callbacks (e.g. `onNavigateNext`).

## Key Decisions
- **Offline First & Mock Repositories**: In v1, the app relies purely on mock data. All domain interfaces are bound to Mock implementations in `AppModule`.
- **Zero Dependencies Cost**: Uses purely open-source libraries and no paid APIs (no Firebase, no Google Maps API keys).

## Dependency List & Versions
- Kotlin 1.9.23
- Compose BOM 2024.04.01
- Navigation Compose 2.7.7
- Hilt 2.51.1
- Room 2.6.1
- DataStore 1.1.0
- WorkManager 2.9.0
