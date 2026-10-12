# Local Deals Finder: Final Manual Regression Checklist

> End-to-end quality assurance and verification test suite for **Local Deals Finder** release build (`com.localdeals.finder` v1.0.0).
> Follow these steps in order to validate all 3 user roles, persistence, security, and edge cases before Play Store release.

---

## 1. First-Launch & Onboarding Flow

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 1.1 | **Cold Launch & Splash** | Launch app fresh or after clearing app data. | Branded deal-orange pin + tag splash appears smoothly via SplashScreen API, transition to Onboarding. | [ ] |
| 1.2 | **3-Slide Onboarding** | Swipe through Slide 1 (Nearby Deals), Slide 2 (Direct Chat & Orders), and Slide 3 (100% Offline & Private). | Animated dots update correctly; smooth swipe transitions. | [ ] |
| 1.3 | **Skip Onboarding** | Tap "Skip" on Slide 1 or 2. | Immediately navigates to Login screen; onboarding flag marked completed in DataStore. | [ ] |
| 1.4 | **Get Started Button** | Navigate to Slide 3 and tap "Get Started". | Navigates to Login screen. Next app launch skips Onboarding automatically. | [ ] |

---

## 2. Authentication & Session Persistence (Customer, Seller, Admin)

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 2.1 | **Customer Login** | Log in with `rahim@local.app` / `User@123` (or `alice@example.com` / `pass123`). | Navigates to Location Permission / Home screen with customer bottom navigation. | [ ] |
| 2.2 | **Encrypted Session Persistence** | Force-close the app from recent apps and relaunch. | Session remains active without re-authenticating; opens directly to Home. Session token encrypted with AES-GCM. | [ ] |
| 2.3 | **Registration** | Tap "Register" on Login screen; enter name, valid email, and password. | Account created in Room database with salted hash; auto-logs in. | [ ] |
| 2.4 | **Seller Login** | Log in with `user_s1@local.app` / `Seller@123`. | Directly routes to **Seller Dashboard**; role guard enforces merchant access. | [ ] |
| 2.5 | **Admin Login** | Log in with `admin@local.app` / `Admin@123`. | Directly routes to **Admin Panel**; role guard enforces admin-only access. | [ ] |
| 2.6 | **Role Guard Enforce** | As customer, attempt to access Admin route. | Access denied; Displays "Unauthorized. You do not have permission to view this page." | [ ] |
| 2.7 | **Logout** | In Profile, tap "Logout". | Clears encrypted session in DataStore; navigates back to Login screen. | [ ] |

---

## 3. Location, Discovery & Home Screen

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 3.1 | **Location Permission (Granted)** | On first login, grant ACCESS_FINE_LOCATION. | App acquires GPS/Network fix, updates header to current Dhaka district. | [ ] |
| 3.2 | **Location Fallback (Denied)** | Deny location permission. | Dialog appears allowing manual selection of predefined Dhaka areas (Dhanmondi, Gulshan, Mirpur, Uttara, etc.). | [ ] |
| 3.3 | **Category Filter** | Tap "Food", "Fashion", "Electronics" chips. | Navigates to Category screen; displays filtered deals sorted by distance. | [ ] |
| 3.4 | **Hot Deals Near You** | Inspect Hot Deals row. | Deals displayed sorted by Haversine distance from selected coordinates. | [ ] |
| 3.5 | **Ending Soon Countdown** | Inspect Ending Soon row. | Live countdown ticks downward; expired deals are excluded from display. | [ ] |

---

## 4. Search, Filter & Sanitization

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 4.1 | **Live Search Debounce** | Type "Pizza" in search bar. | Debounced search triggers after 500ms without UI jank. | [ ] |
| 4.2 | **Input Sanitization (XSS & SQLi)** | Enter `<script>alert('hack')</script>Pizza'; DROP TABLE deals;--`. | Dangerous tags/characters stripped cleanly; app searches safely for "Pizza DROP TABLE deals--". | [ ] |
| 4.3 | **Filter Bottom Sheet** | Open filters; adjust price range, discount %, distance, and min rating. | Filter chips update dynamically; deals match multi-parameter query. | [ ] |
| 4.4 | **Recent Searches** | Perform search, clear query, tap search bar. | Displays recent searches list; tapping chip searches term; "Clear All" clears history. | [ ] |

---

## 5. Offline Interactive Map

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 5.1 | **Procedural Map Rendering** | Open Map tab. | Custom Compose canvas renders procedural grid and deal pins with price badges. | [ ] |
| 5.2 | **Gestures** | Pinch-to-zoom, drag-pan, and double-tap zoom. | Smooth 60fps rendering without memory allocation spikes or OOM. | [ ] |
| 5.3 | **Pin Interaction** | Tap any pin on canvas. | Bottom sheet pops up with deal details, discount, and "View Details" / "Get Directions" buttons. | [ ] |
| 5.4 | **List / Map Toggle** | Tap List/Map icon in top bar. | Toggles seamlessly between interactive canvas and list view. | [ ] |

---

## 6. Deal Details, Wishlist & Notifications

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 6.1 | **Deal Details Display** | Tap any deal card. | Photo, title, discount badge, savings calculation, stock status, seller info, and reviews render correctly. | [ ] |
| 6.2 | **Wishlist Toggle** | Tap heart icon on deal. | Deal added to Wishlist; heart fills; alert notification toggle available in Wishlist screen. | [ ] |
| 6.3 | **Share Intent** | Tap share icon. | Opens Android system share sheet with deal summary and price. | [ ] |
| 6.4 | **Out of Stock Guard** | View out-of-stock deal. | "Request Order" button is disabled; badge indicates "Out of Stock". | [ ] |
| 6.5 | **Expiring Deals Worker** | Trigger background WorkManager job. | System notification alerts user about expiring wishlist deal. | [ ] |

---

## 7. Chat & Order Lifecycle

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 7.1 | **Direct Chat** | Tap "Chat" button on deal details or store profile. | Opens ChatScreen; auto-replies simulate seller assistance with contextual responses. | [ ] |
| 7.2 | **Request Order** | From deal details, tap "Request Order"; choose quantity (e.g., 2), enter note. | Order created with status `PENDING`; order appears under "My Orders". | [ ] |
| 7.3 | **Order Cancellation** | On `PENDING` order in My Orders, tap "Cancel Order". | Order status transitions to `CANCELLED`. | [ ] |
| 7.4 | **Atomic Accept Transaction** | Log in as seller (`user_s1@local.app`); open Orders; tap "Accept". | **Atomic Transaction executes:** Order status -> `ACCEPTED`, deal stock decrements by ordered quantity, buyer receives notification. | [ ] |
| 7.5 | **Ready & Complete** | As seller, advance order to `READY` then `COMPLETED`. | Status transitions successfully; completed orders unlock review submission. | [ ] |
| 7.6 | **Atomic Review Transaction** | As buyer, submit 5-star review on completed order. | **Atomic Transaction executes:** Review inserted and seller's average rating recalculated dynamically. | [ ] |

---

## 8. Seller Dashboard & Store Management

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 8.1 | **Seller Overview** | Log in as seller. | Dashboard displays metrics (active deals, pending orders, unread messages, safe canvas chart). | [ ] |
| 8.2 | **Add New Deal** | Tap "Add Deal", enter title, discount %, price, stock, expiry date. | Form validation works; deal added to Room database; appears in store catalog. | [ ] |
| 8.3 | **Delete Deal (Cascade)** | Delete existing deal. | Deal deleted; associated reviews, wishlist links, and order links handled cleanly via DB cascade. | [ ] |
| 8.4 | **Store Profile Edit** | Update store opening hours, phone number, and location pin. | Changes persist in Room database and reflect on public store profile. | [ ] |

---

## 9. Admin Panel & Moderation

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 9.1 | **Overview Metrics** | Log in as `admin@local.app`. | Displays total users, total sellers, active deals, and tracked analytics events. | [ ] |
| 9.2 | **Block User** | In Users tab, block a customer account. | User marked `isBlocked = true`. Blocked user cannot log in and sees error message. | [ ] |
| 9.3 | **Suspend Seller** | In Sellers tab, suspend a store. | Store deals marked inactive; customers cannot place new orders with this store. | [ ] |
| 9.4 | **Manage Categories** | Delete or add category. | Category deleted; deals seamlessly fallback to "misc" category. | [ ] |
| 9.5 | **Review Moderation** | Inspect reported reviews; dismiss or delete. | Offensive review removed from database; seller rating recomputed. | [ ] |

---

## 10. Localization, Quality & Reset Demo Data

| # | Test Scenario | Steps | Expected Result | Pass / Fail |
|---|---------------|-------|-----------------|:-----------:|
| 10.1 | **Language Selector** | In Profile, switch between English and বাংলা. | All localized strings update immediately with accurate translations. | [ ] |
| 10.2 | **Accessible Touch Targets** | Test all buttons, icons, and chips. | All interactive touch targets measure >= 48dp with clear visual feedback. | [ ] |
| 10.3 | **Reset Demo Data** | In Profile, tap "Reset Demo Data / Clear All"; confirm. | Room DB cleared in a single transaction and reseeded to factory defaults (40 deals, 12 sellers, categories). | [ ] |
| 10.4 | **R8 Release Bundle** | Build release AAB (`./gradlew bundleRelease`). | Compiles with R8 minification and resource shrinking, bundle size ~4.27 MB, zero crash on startup. | [ ] |
