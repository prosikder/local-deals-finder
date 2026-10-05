# Local Deals Finder: Antigravity Prompt Pack (15 Steps)

Same structure as RepBattle: master rules once, then ONE prompt at a time.
Extra: a RESUME system, so you can close this window and continue in a new one without losing anything.

---

## HOW THE RESUME SYSTEM WORKS

The repo is the memory, not the chat window.

- `docs/PROGRESS.md` holds the pinned project context, the 15-step checklist, the changelog, known issues, and a "Resume Note" for the next window.
- Every prompt starts with a RESUME CHECK and ends by updating `docs/PROGRESS.md`.
- In a NEW window: paste the MASTER RULES, then paste the NEW WINDOW MESSAGE below. Then continue with the next prompt.

---

## NEW WINDOW MESSAGE (paste right after Master Rules in any new window)

```
RESUME SESSION. This is a continuation, not a new project.
1. Read README.md, docs/ARCHITECTURE.md and docs/PROGRESS.md fully.
2. Inspect the actual code to verify what PROGRESS.md claims is really done.
3. Report: the last completed step, the current step, known issues, and any mismatch between the docs and the code.
4. Do NOT rebuild, rename, or refactor anything already done. Do NOT start the next step.
5. Wait for me to paste the next prompt.
```

---

## MASTER RULES (paste once at the start of every session / window)

```
You are building "Local Deals Finder", a native Android app, for a solo developer.

Concept: "Where are the good offers near me?" Users discover local deals sorted by distance and discount, view them on a map, search and filter, chat with sellers, send order requests, and leave reviews. Sellers post and manage deals from a dashboard. A minimal Admin role moderates the platform.

Stack: Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Hilt, Room, DataStore, Navigation Compose, Coroutines/Flow, WorkManager.

Hard constraints:
- $0 dependency cost. Only free libraries. No paid APIs or assets. Use only royalty-free/CC0 or self-generated assets (vector drawables, gradients, Canvas art).
- ZERO external services in v1: no Firebase, no backend, no Google Maps API key, no Play Services location, no payments. The app works 100% offline.
- Chat, orders, and notifications are local simulations. Define interfaces (DealRepository, ChatRepository, OrderRepository, AuthRepository, LocationRepository) with fake in-memory implementations first, so Room-backed (and later real network) versions can be swapped in without refactoring.
- Three roles in one app: User, Seller, Admin, with navigation role guards.
- minSdk 26, package com.localdeals.finder.

Design system:
- Fresh deals-oriented palette: deal-orange primary (#FF6B1A), teal secondary (#14B8A6), warm off-white surfaces for light mode, a true dark theme, and a red/green for discount and stock states.
- Clear typography hierarchy, bold price and discount numerals, rounded cards, short and friendly animations.

Screens: Splash, Login/Register, Location, Home, Search, Category, Map, Deal Details, Seller Store, Chat, Order Request, My Orders, Wishlist, Reviews, Notifications, Profile, Seller Dashboard, Admin Panel.

Working rules:
- I will give you ONE prompt at a time. Finish only that step, make sure the project builds and runs, then stop.
- Do not build features from later steps.
- Clean, commented, production-style code. No placeholder TODOs for the current step.

RESUME RULE (applies to every prompt, and to any new window):
- The repo is your only memory. Before doing anything, read docs/PROGRESS.md, docs/ARCHITECTURE.md and README.md, and check the real code.
- Confirm the previous step is actually done and the project builds. If it is not, tell me and fix only that first.
- Never redo, rename, or restructure work from earlier steps unless a bug requires it, and then say so in the changelog.
- Follow the existing package structure, naming, UiState pattern, and Hilt bindings exactly.

DOCS RULE: create and maintain these files in the repo root, updating them at the end of EVERY step:
  1. README.md: what the app is, how to run, current feature list.
  2. docs/ARCHITECTURE.md: package structure, data flow, key decisions, dependency list with versions.
  3. docs/PROGRESS.md: a "Project Context (pinned)" section (app name, package, stack, constraints, key decisions), a checklist of all 15 steps (done / current / next), a changelog entry per step, known issues, manual test results, and a "Resume Note" (exact next prompt number, anything unfinished, anything the next window must know).

After each step, report: files created/changed, how to run, what I should test manually, and what the next step is.

Confirm you understand, then wait for Prompt 1.
```

---

## Prompt 1: Project Foundation & Architecture

```
PROMPT 1: Project scaffold.
(RESUME CHECK: if the repo is empty, this is a fresh start. Otherwise follow the RESUME RULE.)

Create the Android project "Local Deals Finder" (package com.localdeals.finder, minSdk 26, Kotlin, Compose, Material 3).

Build:
1. Package structure: data (local, repository, mock), domain (model, usecase), ui (theme, components, screens, navigation), di, util.
2. Version catalog (libs.versions.toml) with all current stable versions. Add Hilt, Room, DataStore, Navigation Compose, Coroutines, WorkManager, and the Compose BOM only. Do not add any other libraries.
3. Hilt setup, Application class, and a clean MainActivity with edge-to-edge.
4. Domain models (no Android dependencies): User, Seller, Category, Deal, Review, ChatMessage, OrderRequest, AppNotification, and a Role enum (USER, SELLER, ADMIN).
5. Repository interfaces for each model, with fake in-memory implementations bound through Hilt.
6. A UiState pattern (sealed class: Loading, Success, Empty, Error) and a base ViewModel convention, documented in ARCHITECTURE.md.
7. Create README.md, docs/ARCHITECTURE.md, and docs/PROGRESS.md as defined in the master rules (including the pinned context, the 15-step checklist, and the Resume Note).

Update the docs and stop when it builds and runs an empty themed screen.
```

## Prompt 2: Navigation & Design System

```
PROMPT 2: Navigation and design system.
(RESUME CHECK first, per the RESUME RULE. Prompt 1 must be done.)

Build:
1. Theme: the palette from the master rules, light + dark, Typography, shapes, and a LocalDealsTheme.
2. Reusable components: DealCard, StoreCard, CategoryChip, SearchBar, PrimaryButton, RatingBar, DiscountBadge, CountdownText, EmptyState, LoadingSkeleton, ErrorState, TopBar.
3. Navigation Compose with sealed Routes and placeholder screens (each shows its name): Splash, Login, Register, LocationPermission, Home, Map, Search, Category, DealDetails, SellerStore, Chat, OrderRequest, MyOrders, Review, Wishlist, Notifications, Profile, SellerDashboard, AdminPanel.
4. Bottom navigation for the User role (Home, Map, Search, Wishlist, Profile), hidden on detail, chat, and order screens.
5. A role-aware navigation graph (User / Seller / Admin) with a RoleGuard helper, wired but not yet functional.
6. Wire the flow: Splash → Login → Location → Home.
7. A component gallery screen (debug-only) to preview every component in light and dark.

Update the docs (including PROGRESS.md and the Resume Note) and stop when it builds and navigates between placeholders.
```

## Prompt 3: Login & User Profile

```
PROMPT 3: Local auth and profile (a Room-ready AuthRepository interface; DataStore for the session).
(RESUME CHECK first. Prompts 1-2 must be done.)

Build:
1. Register/Login with email + password (validated, with clear inline errors), password hashing with SHA-256 + a per-user salt, and role selection (User / Seller) at registration.
2. Session persistence in DataStore, logout, and a seeded Admin account (admin@local.app / Admin@123, documented in the README as a dev-only credential).
3. Splash decides the destination: Login, LocationPermission, or the role's home.
4. Profile screen: edit name, phone, an avatar with initials, preferred search radius (1/3/5/10 km), language placeholder, and logout.
5. A use case for each action (RegisterUser, LoginUser, UpdateProfile), with unit tests for validation and hashing.
6. Handle the process-death and back-stack edge cases correctly (no login screen flash for a logged-in user).

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 4: Location Permission

```
PROMPT 4: Location (no Play Services).
(RESUME CHECK first. Prompts 1-3 must be done.)

Build:
1. A permission screen explaining why location is needed, requesting ACCESS_FINE/COARSE_LOCATION through the runtime API, and handling granted, approximate-only, denied, and "don't ask again" (deep-link to app settings), all in Compose.
2. A LocationRepository using the framework LocationManager: last known location, a single fresh fix with a timeout, and a Flow of the current location. Handle GPS-off with a prompt to open location settings.
3. A manual fallback: a city/area picker with preset coordinates (Dhaka areas such as Dhanmondi, Gulshan, Mirpur, Uttara, Banani, Motijheel), stored in DataStore.
4. A shared CurrentLocation holder (granted GPS or the manual pick) that all screens use, plus a "Change location" bottom sheet.
5. A Haversine distance util and a bearing helper in util/, with unit tests.
6. Handle the lifecycle properly: stop updates in the background and no leaks.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 5: Home Screen

```
PROMPT 5: Home (mock data behind the DealRepository, seeded with realistic Bangladeshi data).
(RESUME CHECK first. Prompts 1-4 must be done.)

First, create the mock dataset: 8 categories (Food, Grocery, Fashion, Electronics, Beauty, Health, Services, Home), 12 sellers and 40 deals around Dhaka with real-looking coordinates, BDT prices, varied discounts, and some deals expiring soon. Use vector/gradient placeholders for images (no network images).

Build:
1. A location header with a change option, and a search bar that opens Search.
2. Horizontal category chips.
3. "Hot deals near you" (sorted by a score of distance + discount), "Ending soon" with a live countdown, and a "Nearby stores" list.
4. Pull-to-refresh and skeleton loading, with empty/error states.
5. HomeViewModel with a UiState sealed class and StateFlow, with no logic in composables, plus a GetNearbyDeals use case (unit-tested, including distance sorting).
6. Subtle entrance animations and LazyList keys.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 6: Search & Categories

```
PROMPT 6: Search and categories.
(RESUME CHECK first. Prompts 1-5 must be done.)

Build:
1. Search screen: live search by deal or store name with debounce, recent searches saved in DataStore (with clear), and suggestions.
2. Filters in a bottom sheet: category, max distance, price range, min rating, and min discount %. Sort: nearest, biggest discount, cheapest, top rated. Show active filter chips with a reset.
3. Category screen listing deals per category, with the same filters and sort.
4. Helpful empty and no-result states ("Try widening the distance").
5. SearchViewModel with debounced Flow, a SearchDeals use case (unit-tested for filters and sorts), and state surviving rotation.
6. Wire Home → Search → Category flows.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 7: Deal & Product Details

```
PROMPT 7: Deal details.
(RESUME CHECK first. Prompts 1-6 must be done.)

Build:
1. Deal Details: an image area (gradient/vector), title, price with the original struck through, a discount badge, stock left, a live expiry countdown, distance, description, a seller mini-card, a rating summary, and a reviews preview.
2. Actions: Wishlist toggle (an in-memory repository for now), Chat with seller, Order request (these lead to placeholders until Prompt 10), and Share through a system share intent.
3. Out-of-stock, expired, and "ending soon" states, disabling the actions and showing a clear badge.
4. A related-deals row ("More from this store", "Similar deals").
5. DealDetailsViewModel with the stock/expiry logic in a use case (unit-tested for expiry and stock rules).
6. Navigation: Home/Search/Category → Deal Details, with a shared-element or fade transition.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 8: Map & Nearby Deals (offline, no API key)

```
PROMPT 8: Offline custom map. No map SDK, no API key.
(RESUME CHECK first. Prompts 1-7 must be done.)

Build with Compose Canvas:
1. A stylized map canvas (roads, blocks, and a grid drawn procedurally) with pan, pinch-zoom, and double-tap zoom, plus a recenter button.
2. A "You are here" marker from CurrentLocation, with deal/store pins placed by lat/lng projected onto canvas coordinates (write the projection in util/ with unit tests).
3. Pins colored by category, with a discount badge. Tapping a pin opens a bottom sheet with a deal preview and a "View details" button.
4. A radius circle matching the user's search radius, and a category filter row on top.
5. An "Open in Maps" button firing a geo: intent for directions in the user's installed maps app, with a graceful fallback if none is installed.
6. A Map/List toggle using the same data from the repository, with pin clustering when zoomed out.
7. Keep it performant: remember, derivedStateOf, no allocations in the draw loop, and cull off-screen pins.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 9: Seller Store Profile

```
PROMPT 9: Seller store profile.
(RESUME CHECK first. Prompts 1-8 must be done.)

Build:
1. Store header: a banner (gradient/vector), name, description, address, distance, an open/closed status computed from opening hours, and the rating summary.
2. Tabs: Deals (active deals grid), Reviews (list with the average and a rating distribution bar), and About (hours, address, a "Get directions" geo intent, and a call intent).
3. Chat and Order buttons (leading to placeholders until Prompt 10), and a Follow/Favorite store toggle (in-memory).
4. Handle a store with no deals, no reviews, or suspended status.
5. SellerStoreViewModel plus an IsStoreOpen use case (unit-tested, including the overnight-hours edge case).
6. Navigation: Deal Details → Seller Store → Chat/Order, and Home "Nearby stores" → Seller Store.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 10: Chat & Order Request (local simulation)

```
PROMPT 10: Chat and orders, local only (in-memory repositories; Room comes in Prompt 13).
(RESUME CHECK first. Prompts 1-9 must be done.)

Build:
1. A chat list and chat screen: message bubbles, timestamps, read state, and an auto-reply simulator (the seller replies after 2-4 seconds with context-aware canned responses such as "Yes, it's in stock!"), plus a typing indicator.
2. Order request flow: choose a quantity, a pickup/delivery note, phone confirmation, and a summary, then submit. Lifecycle: Pending → Accepted → Ready → Completed / Rejected / Cancelled. No payment.
3. A "My Orders" screen with status chips and cancel for pending orders.
4. Stock decrements when the order is accepted, with validation (quantity ≤ stock) and protection against double submit.
5. A simulator that moves an order through statuses on a timer for demo purposes (a debug toggle), creating a local notification entry on each status change (data only).
6. An OrderLifecycle state machine in domain (unit-tested: valid and invalid transitions, stock rules).

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 11: Wishlist, Reviews & Notifications

```
PROMPT 11: Engagement features.
(RESUME CHECK first. Prompts 1-10 must be done.)

Build:
1. Wishlist screen with remove, a price/expiry alert toggle, sorting, and empty state.
2. Reviews: after a Completed order the user can rate 1-5 and write a comment (one review per order, validated). Update the seller's average rating automatically (in a use case, unit-tested). Show reviews on the store and deal pages, with a report-review option.
3. Notifications: an in-app list (read/unread, mark all read, swipe to delete) plus real Android system notifications through NotificationManager with a channel, and the POST_NOTIFICATIONS runtime permission on Android 13+ (graceful if denied).
4. A WorkManager periodic job (local) notifying about wishlist deals expiring within 24 hours and new deals nearby, with battery-friendly constraints.
5. Notification tap deep-links to the right screen (order, chat, deal), including from a cold start.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 12: Seller Dashboard & Admin Panel

```
PROMPT 12: Seller and Admin roles.
(RESUME CHECK first. Prompts 1-11 must be done.)

SELLER:
1. Dashboard summary: active deals, pending orders, unread messages, views, and a 7-day chart drawn with Canvas (no chart library).
2. Add/Edit/Delete Deal with form validation: title, description, price, discount, stock, an expiry date/time picker, a category, and picking an image from the gallery with the photo picker (store the URI).
3. Stock management (quick +/-, an automatic "sold out"), orders management (accept/reject/ready/complete), a seller inbox, and a store profile edit with the location pin set from the current location.
4. Basic analytics per deal: views, wishlist adds, orders, and conversion.

ADMIN (minimal control):
5. Panels: Users (block/unblock), Sellers (approve/suspend), Deals (remove), Reports/Reviews (moderate), Categories (add/edit/delete), and an overall analytics summary.

Enforce role guards: a user must not reach seller/admin routes, and a blocked user is logged out. Add unit tests for the guards and the deal validation.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 13: Local Offline Database

```
PROMPT 13: Room persistence.
(RESUME CHECK first. Prompts 1-12 must be done.)

Build Room entities, DAOs, and repositories for: User, Seller, Category, Deal, Review, Wishlist, ChatMessage, OrderRequest, Notification, RecentSearch, and DealStats (views, wishlist adds).

Also:
1. Replace ALL in-memory repositories with Room-backed ones, keeping the interfaces, with domain mappers separate from entities.
2. A first-launch seeder (the seed data from Prompt 5 plus the Admin account) that runs once, in a transaction, on a background thread.
3. A DealWithSeller query sorted by distance from the current location (a bounding-box prefilter plus an exact Haversine sort), with indices on the hot columns.
4. Transactions for the critical flows: order accepted (status + stock + notification), review submitted (review + rating update), and deal deleted (cascade).
5. Database version 1 with exportSchema on and a documented migration strategy with a tested stub migration.
6. A "Clear all data / reset demo data" option in the Profile (with confirmation).
7. DAO tests and migration tests with an in-memory database.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 14: Security, Analytics, Testing & Optimization

```
PROMPT 14: Hardening.
(RESUME CHECK first. Prompts 1-13 must be done.)

Do:
1. Security: no hardcoded secrets (the dev admin credential only in a debug seed), R8/ProGuard enabled with a keep rules review (Room, Hilt, WorkManager), allowBackup/data-extraction rules, session encryption (DataStore + Android Keystore or EncryptedSharedPreferences), input sanitization, no personal data in release logs, and a navigation role guard audit.
2. Analytics: a local-only AnalyticsTracker interface (an event log in Room with a real implementation, and no third-party SDK) feeding the Seller and Admin analytics screens.
3. Testing: ViewModel/use-case tests (search, order lifecycle, distance sort, rating update), Room DAO tests, and Compose UI tests for Login, Search, and the Order flow.
4. Optimization: LazyList keys, image/memory check, startup time, a recomposition audit, a baseline profile if feasible, and a shrink + minify release build with a size report.
5. Quality: accessibility (content descriptions, 48dp touch targets, contrast), dark mode check, tablet/landscape basic support, an empty/error state audit, and string resources extracted for English + Bengali with a language option.

Update the docs (including PROGRESS.md and the Resume Note) and stop.
```

## Prompt 15: Final Integration & Play Store Release

```
PROMPT 15: Release prep.
(RESUME CHECK first. Prompts 1-14 must be done.)

Do:
1. Final integration pass: run through every flow (User, Seller, Admin) end to end, fix bugs, and remove debug-only screens from release builds.
2. Branding: an app icon + adaptive icon + monochrome themed icon (vector, a location pin + price tag motif in deal-orange), a SplashScreen API splash, and a 3-slide first-launch onboarding.
3. Release engineering: versionCode/versionName, a local keystore signing config with safe-storage instructions (Play App Signing recommended), an AAB build command, and a pre-launch crash check.
4. Play Console pack (docs/PLAY_STORE.md):
   - Store listing copy (EN + BN): a short description, a full description, and keyword ideas.
   - Screenshot list and a feature graphic brief.
   - Privacy policy text (location used on-device for distance sorting, with no data leaving the device in v1).
   - Data safety form answers and a permissions justification (location, notifications).
   - Content rating notes and a closed-testing plan (12 testers for 14 days if it's a new personal developer account).
5. A "v2 roadmap" doc (docs/ROADMAP.md): Firebase sync and a real backend, paid promotions, seller subscriptions via Play Billing, and real chat, each noted as a swap-in behind the existing repository interfaces.
6. A final manual regression checklist (docs/REGRESSION.md), finalize README.md and ARCHITECTURE.md, and mark all 15 steps done in PROGRESS.md.

Stop after the checklist is delivered.
```

---

## Running tips

- Every step: build, run, test manually, then `git commit -m "prompt-N done"`. The commit plus PROGRESS.md is your real backup.
- Window full or closed? New window: Master Rules, then the NEW WINDOW MESSAGE, wait for the report, then paste the next prompt.
- If a step stopped halfway, say so in the new window: "Prompt 8 is partially done, finish only that." The resume check will find what is missing.
- Run on a real phone after Prompts 4, 8, and 11.
- Prompts 12 and 13 are the heaviest. Split 12 into Seller and Admin, and 13 into entities/DAOs and then the repository swap and seeder.
- The dev admin credential is for testing only. It must never ship in the release build.
