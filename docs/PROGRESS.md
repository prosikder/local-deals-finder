# PROGRESS.md: Local Deals Finder

> This file is the project's memory. Any new chat window must read it first.
> Copy it to `docs/PROGRESS.md` in the repo before Prompt 1. Antigravity updates it at the end of every step.

## Project Context (pinned)

- **App:** Local Deals Finder ("Where are the good offers near me?")
- **Package:** com.localdeals.finder | **minSdk:** 26
- **Stack:** Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Hilt, Room, DataStore, Navigation Compose, Coroutines/Flow, WorkManager
- **Roles:** User, Seller, Admin (with navigation role guards)
- **Constraints:** $0 dependency cost. No Firebase, no backend, no Maps API key, no Play Services location, no payments. 100% offline in v1. Chat, orders and notifications are local simulations.
- **Architecture rule:** Repository interfaces first with in-memory fakes, then Room-backed implementations in Prompt 13.
- **Design:** deal-orange #FF6B1A primary, teal #14B8A6 secondary, light + dark theme.
- **Working rule:** ONE prompt at a time. Never build later steps early. Never refactor finished steps without a bug reason.

## Key Decisions

- Created `BaseViewModel` and `UiState` in `com.localdeals.finder.ui` to standardize state management across screens.
- Created Mock implementations of all repositories to fulfill the offline-first requirement.

## Step Checklist

| # | Step | Status |
|---|------|--------|
| 1 | Project Foundation & Architecture | done |
| 2 | Navigation & Design System | done |
| 3 | Login & User Profile | done |
| 4 | Location Permission | done |
| 5 | Home Screen | done |
| 6 | Search & Categories | done |
| 7 | Deal & Product Details | done |
| 8 | Map & Nearby Deals (offline) | NEXT |
| 9 | Seller Store Profile | pending |
| 10 | Chat & Order Request (local) | pending |
| 11 | Wishlist, Reviews & Notifications | pending |
| 12 | Seller Dashboard & Admin Panel | pending |
| 13 | Local Offline Database (Room) | pending |
| 14 | Security, Analytics, Testing & Optimization | pending |
| 15 | Final Integration & Play Store Release | pending |

Status values: done / current / next / pending

## Changelog

- **Step 1 (2026-10-05):** Scaffolding project with Gradle versions catalog, Hilt setup, Clean Architecture packages, Domain Models, Repository interfaces + Mock implementations, UiState/BaseViewModel, and Material 3 custom Theme. Edge-to-edge MainActivity is working.
- **Step 2 (2026-10-06):** Added UI Shapes and Navigation system. Built reusable UI components (DealCard, StoreCard, TopBar, PrimaryButton, States, Badges). Created placeholder Compose screens and wired up NavGraph with BottomNavigation. Added a Component Gallery.
- **Step 3 (2026-10-06):** Implemented Local Auth with SHA-256 password hashing and DataStore session persistence. Added `AuthUseCases` with JUnit tests. Created `LoginScreen`, `RegisterScreen`, and `ProfileScreen`. Updated `SplashViewModel` to auto-route based on session state. Added seeded Admin account (`admin@local.app`).
- **Step 4 (2026-10-07):** Added Location Permission handling with `LocationRepositoryImpl` using `LocationManager`. Handled granted, denied, GPS off states, and manual fallback using DataStore to store predefined Dhaka areas. Implemented `LocationUtils` (Haversine distance, bearing) with unit tests. *Fixes*: Removed `suspend` modifier from `isLocationEnabled` interface method to fix build errors, and changed `LocationViewModel` to extend `androidx.lifecycle.ViewModel` directly (Hilt KSP issue with `BaseViewModel` generics).
- **Step 5 (2026-10-07):** Created `MockData` with realistic Dhaka coordinates, BDT prices, and 40 deals across 8 categories and 12 sellers. Built `HomeScreen` with location header, search bar, horizontal category chips, hot deals sorted by distance/discount, ending soon deals with live countdown, and nearby stores. Added `GetNearbyDealsUseCase` (with unit tests) and `HomeViewModel` managing `UiState` via `StateFlow` with pull-to-refresh. Updated `EmptyState` to support action buttons.
- **Step 6 (2026-10-07):** Added `SearchScreen` and `CategoryScreen`. Implemented `SearchDealsUseCase` supporting category, price, discount, rating, and distance filters, along with sorting (Nearest, Biggest Discount, Cheapest, Top Rated). Added active filter chips in `SearchScreen` and updated `FiltersBottomSheet` to include a minimum rating filter. Updated `DataStoreManager` to save and clear recent searches. Connected navigation from Home to Search and Category flows.
- **Step 7 (2026-10-07):** Implemented `DealDetailsScreen` with full data fetching via `DealDetailsViewModel` (incorporating `DealStatus`, wishlist, location, and reviews). Added fade and slide navigation transitions. Included actions for wishlist (via `WishlistRepository`), chat navigation, and share intents. Handled out-of-stock and expired deal states dynamically. Displayed a "related deals" section. Added `GetDealStatusUseCase` with unit tests for determining deal states (ACTIVE, ENDING_SOON, EXPIRED, OUT_OF_STOCK).
- **Step 8 (2026-10-08):** Implemented an offline custom map in `MapScreen` using Compose `Canvas`. Developed `MapProjection` utility (with unit tests) to translate between geographic coordinates and canvas pixels. Features include procedural grid rendering, pinch-to-zoom, pan, double-tap zoom, and map recentering. Pinned deals include discount badges, are culled when off-screen, and trigger a bottom sheet on tap with "View Details" and "Directions" (`geo:` intent) buttons. Added Map/List toggle, category filter row, and drawn search radius around the user.
- **Debug Task (2026-10-09):** Added a debug-only crash reporter in `LocalDealsApplication` that intercepts uncaught exceptions and saves the stack trace to `last_crash.txt`. Upon app launch in `MainActivity`, an `AlertDialog` displays the crash log with options to dismiss (delete the file) or copy it to the clipboard. Also implemented an `ActivityManager.getHistoricalProcessExitReasons` check (for API 30+) to append OS-level process exit reasons (e.g. CRASH, ANR) to the crash dialog.
- **Map Bug Fix (2026-10-09):** Fixed an OutOfMemory (OOM) crash in `MapScreen` by correcting the usage of `derivedStateOf` for clustering. Removed gesture-driven variables (`centerLat`, `centerLng`, `zoom`) from the `remember` keys, which were previously causing massive reallocations on every frame and subsequently trashing memory.
- **Map Bug Fix 2 (2026-10-09):** Fixed an `IllegalArgumentException: maxWidth must be >= minWidth` crash that occurred when calling `drawText` near canvas edges. The native `drawText(String)` function calculates internal constraints by subtracting the offset from the canvas size. To completely bypass this buggy internal math, `DrawScope.safeDrawText` was rewritten to manually measure text via `TextMeasurer.measure` without constraints and draw the `TextLayoutResult` directly.
- **Step 9 (2026-10-09):** Implemented `SellerStoreScreen` with store profile tabs (Deals, Reviews, About). Features include an open/closed store status computed by a new `IsStoreOpenUseCase` (which properly supports overnight edges and is fully unit-tested), rating distribution summary bars, actionable "Get Directions" and "Call" intent buttons, and an in-memory favorite toggle backed by a new `StoreFavoriteRepository`. Wired navigation properly from `DealDetailsScreen` and `HomeScreen` to `SellerStoreScreen`.
## Known Issues

- None

## Known Pitfalls

- **Canvas `drawText` Crash:** Never call `drawText` near a canvas edge without clamping or passing an explicit `size`. If the available space calculated internally by `drawText` becomes negative (e.g., when drawing objects in a culling buffer zone slightly outside the viewable canvas), it will throw `IllegalArgumentException: maxHeight must be >= than minHeight`. Always use a safe wrapper like `DrawScope.safeDrawText` that uses `coerceAtLeast(0f)` for available width and height.

## Manual Test Results

- **Step 1:** Ran gradle build successfully, the project successfully compiles, the empty screen shows "Local Deals Finder" with the custom theme.
- **Step 2:** The app compiles successfully. The app launches and navigates Splash -> Login -> Location -> Home. The Component Gallery successfully displays all reusable UI components.
- **Step 3:** The app successfully runs unit tests for validation and hashing. The app can login with the seeded Admin account or register a new user, and it persists the session across restarts using DataStore. Profile allows editing details.
- **Step 4:** Haversine and Bearing unit tests pass. The app prompts for location permissions and handles GPS vs manual selection fallback. Coordinates are successfully retrieved via LocationManager or DataStore.
- **Step 5:** `GetNearbyDealsUseCase` unit tests pass (verifies correct sorting by discount and distance score). The app compiles and runs. `HomeScreen` correctly displays hot deals, categories, and nearby stores, navigating appropriately.
- **Step 6:** `SearchDealsUseCase` unit tests pass (verifies search by query and filters/sorting). Live search works with debounce. Recent searches are saved and displayed when the search bar is active and empty. `FiltersBottomSheet` properly applies state including min-rating. Active filters render correctly with a clear button. Navigation from `Home` to `Search` and `Category` is functional.
- **Step 7:** `DealDetailsScreen` loads fully. Live countdown ticks properly. Share intent triggers contextually. UI appropriately disables "Request Order" for out of stock / expired items, and shows status chips (Ending Soon, Out of Stock, Expired). `GetDealStatusUseCase` passes all stock and expiration rule tests. Navigation supports custom transitions.
- **Step 8:** `MapProjectionTest` passes, accurately translating between pixel coordinates and lat/lng. The map renders performantly with off-screen culling and `derivedStateOf`. Tap detection resolves to the nearest pin. Map/List toggle changes view states successfully, and filter chips dynamically update displayed deals. Intents to open external map apps work with graceful fallbacks.

## Resume Note

- **Last completed step:** Step 8
- **Next prompt:** Prompt 9
- **Unfinished work / things the next window must know:** `LocationViewModel` directly extends `androidx.lifecycle.ViewModel` instead of `BaseViewModel<T>` to avoid Hilt KSP generic resolution issues. `DealDetailsScreen` uses a floating "Request Order" button which is currently a no-op (waiting for Order Request prompt).
