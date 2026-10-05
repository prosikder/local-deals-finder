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
| 3 | Login & User Profile | NEXT |
| 4 | Location Permission | pending |
| 5 | Home Screen | pending |
| 6 | Search & Categories | pending |
| 7 | Deal & Product Details | pending |
| 8 | Map & Nearby Deals (offline) | pending |
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

## Known Issues

- None

## Manual Test Results

- **Step 1:** Ran gradle build successfully, the project successfully compiles, the empty screen shows "Local Deals Finder" with the custom theme.
- **Step 2:** The app compiles successfully. The app launches and navigates Splash -> Login -> Location -> Home. The Component Gallery successfully displays all reusable UI components.

## Resume Note

- **Last completed step:** Step 2
- **Next prompt:** Prompt 3
- **Unfinished work / things the next window must know:** None.
