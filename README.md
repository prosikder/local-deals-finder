# Local Deals Finder

"Where are the good offers near me?" Users discover local deals sorted by distance and discount, view them on a map, search and filter, chat with sellers, send order requests, and leave reviews. Sellers post and manage deals from a dashboard. A minimal Admin role moderates the platform.

## Features
- **100% Offline (v1):** Completely standalone application relying on local data. No backend or external cloud services needed to run the UI and demo flows.
- **Three Roles:** User, Seller, Admin
- **Clean Architecture & MVVM:** Robust code structure using modern Android guidelines.
- **Jetpack Compose:** Fully declarative UI using Material 3 and custom themes.

## How to Run
1. Open the project in Android Studio (Jellyfish or newer recommended).
2. Build and run the `app` module on an emulator or physical device running API 26 or higher.
3. No configuration, API keys, or backend setup is needed.

## Current Feature List
- Project scaffolded with Gradle Version Catalogs, Hilt, Room, DataStore, WorkManager, Navigation Compose.
- Domain models and mock repositories implemented.
- Clean MainActivity and LocalDealsApplication.
