# Local Deals Finder

"Where are the good offers near me?" Users discover local deals sorted by distance and discount, view them on a map, search and filter, chat with sellers, send order requests, and leave reviews. Sellers post and manage deals from a dashboard. A minimal Admin role moderates the platform.

## Features
- **Offline-First with 100% Free Cloud Sync:** Complete offline functionality with local Room SQLite persistence, seamlessly enhanced with Google Firebase (Firestore, Firebase Cloud Messaging, Firebase Auth) under the 100% free Spark plan.
- **Three Roles:** User, Seller, Admin
- **Clean Architecture & MVVM:** Robust code structure using modern Android guidelines.
- **Jetpack Compose:** Fully declarative UI using Material 3 and custom themes.

## How to Run
1. Open the project in Android Studio (Jellyfish or newer recommended).
2. Build and run the `app` module on an emulator or physical device running API 26 or higher.
3. **Works instantly out-of-the-box offline:** Includes a starter configuration and demo data.
4. **Optional Cloud Connection:** Connect to your own free Google Firebase project anytime by downloading your `google-services.json` from Firebase Console and placing it in `/app/google-services.json`.

## Current Feature List
- Project scaffolded with Gradle Version Catalogs, Hilt, Room, DataStore, WorkManager, Navigation Compose.
- Domain models and mock repositories implemented.
- Clean Architecture layered application with extensive unit testing.
- Full UI components gallery, Navigation system, and UI Theme.
- Login, registration, role-based routing, and offline session storage via DataStore.
- Mock location handling with user fallback.
- Home screen with hot deals, live countdown timers, categories, and nearby stores.
- Search with multi-filter (distance, price, discount, rating) and category filters.
- Deal Details screen showing product statuses, store info, and actions.
- Offline interactive Custom Map (Canvas-based) with PIN clustering, pinch/zoom.
- Seller public profiles displaying location, operating hours, deals, and reviews.
- Real-time Bidirectional Chat (Room offline persistence + Firebase Firestore live sync).
- Full Order Lifecycle for users & sellers with Firestore real-time synchronization.
- Wishlist and user review system.
- Firebase Cloud Messaging (FCM) & WorkManager background notifications with deep links.
- Seller Dashboard: Manage deals (Add, Edit, Delete with instant Firestore sync), Manage orders (auto-decrementing stock), Store profile edit, and per-deal conversion analytics.
- Admin Panel: Comprehensive dashboard for managing users (block/unblock), sellers (suspend/approve), deals, review reports, categories, and platform analytics.
- Room Local Database: Complete offline persistence with 12 Room entities, 11 DAOs, background first-launch seeder, bounding-box + Haversine distance search, atomic transaction pipelines, and Profile "Reset Demo Data / Clear All" option.
- Hardening: Security (session encryption with KeyStore AES-GCM, input sanitization, PII masking, no cloud backup of local db/prefs), local Room analytics tracker (0 third-party SDKs), 71 tests (Room DAOs, migrations, use cases, and Compose UI tests), optimization with R8 minification, LazyList keys across all screens, and bilingual support (English + Bengali).
- Release Prep (v1.0.0): Official Android SplashScreen API, vector adaptive + monochrome Material You themed icon, 3-slide first-launch onboarding, debug-only screen stripping in release builds, keystore signing configuration, production Android App Bundle build (`./gradlew bundleRelease`), Play Store console metadata pack (`docs/PLAY_STORE.md`), v2 cloud roadmap (`docs/ROADMAP.md`), and regression checklist (`docs/REGRESSION.md`).

## Test Accounts (Seeded)
| Role | Email | Password | Access / Destination |
|:---|:---|:---|:---|
| **Customer** | `rahim@local.app` (or `alice@example.com`) | `User@123` (`pass123`) | Customer Home, Map, Search, Orders, Wishlist |
| **Seller** | `user_s1@local.app` | `Seller@123` | Seller Dashboard (Deals, Orders, Analytics, Store) |
| **Admin** | `admin@local.app` | `Admin@123` | Admin Panel (Users, Sellers, Deals, Categories) |

## Build Commands
```bash
# Run all unit, Robolectric DAO, and Compose tests:
./gradlew testDebugUnitTest

# Build optimized minified release APK:
./gradlew assembleRelease

# Build Google Play release Android App Bundle (AAB):
./gradlew bundleRelease
```

## Documentation
- [Architecture & Design Decisions](docs/ARCHITECTURE.md)
- [Project Step Progress Tracker](docs/PROGRESS.md)
- [Google Play Store Release Pack](docs/PLAY_STORE.md)
- [v2 Technical Roadmap](docs/ROADMAP.md)
- [Final Manual Regression Checklist](docs/REGRESSION.md)
- [সম্পূর্ণ টেস্টিং নির্দেশিকা (বাংলায়)](docs/TESTING_BANGLA.md)
- [Firebase ফ্রি ক্লাউড সেটআপ গাইড](docs/FIREBASE_SETUP.md)
