# Local Deals Finder: Version 2 (v2) Technical Roadmap

> Strategic evolution and production cloud integration plan for **Local Deals Finder**.
> Every cloud feature is designed as a direct swap-in behind the established Clean Architecture domain repository interfaces, preserving all existing UI screens, ViewModels, and unit test contracts.

---

## Architecture Foundation: Clean Architecture Swap-In Strategy

In v1, all repositories adhere strictly to clean domain interfaces (`AuthRepository`, `DealRepository`, `SellerRepository`, `OrderRepository`, `ChatRepository`, `WishlistRepository`, etc.) backed by local Room DAOs.

In v2, transitions to cloud backends will **NOT** require refactoring any ViewModels, Compose screens, or use cases. Instead, Hilt dependency injection modules (`DataModule.kt`) will bind remote or hybrid sync implementations (`SyncDealRepositoryImpl`, `FirebaseChatRepositoryImpl`, etc.) behind the identical interfaces.

```
┌────────────────────────────────────────────────────────┐
│               Jetpack Compose UI Layer                 │
│         (Screens, ViewModels, StateFlow, UiState)      │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                 Domain Use Cases Layer                 │
│    (GetNearbyDeals, SearchDeals, OrderLifecycle, etc.) │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│              Domain Repository Interfaces              │
│       (DealRepository, ChatRepository, OrderRepo...)   │
└───────────────▲────────────────────────▲───────────────┘
                │                        │
       [v1 Room Offline]        [v2 Cloud / Firebase]
         - AppDatabase            - Firestore / Supabase
         - Local DAOs             - Cloud Messaging (FCM)
         - SQLite Cache           - Google Play Billing
```

---

## 1. Cloud Backend & Real-Time Sync (Firebase / Supabase)

### Target Component: `SyncDealRepositoryImpl` & `SyncSellerRepositoryImpl`
- **Objective:** Enable multi-merchant catalog management across devices and provide cloud-backed deals syncing to local Room SQLite cache.
- **Implementation Strategy:**
  - **Local-First Sync Architecture:** Room remains the single source of truth for the UI layer (preserving instant offline startup and zero latency).
  - Use Firebase Firestore or Supabase PostgreSQL with real-time change data capture (CDC).
  - Background WorkManager sync worker (`CatalogSyncWorker`) to pull newly published deals and invalidate expired ones.
  - Conflict resolution using timestamp-based Last-Write-Wins (LWW) or CRDT-based merging.
- **Repository Interface:**
  - Implements `DealRepository` and `SellerRepository`.
  - Exposes `Flow<List<Deal>>` driven by Room with network sync triggers in parallel coroutine scopes.

---

## 2. Real-Time Chat & Push Notifications (FCM / WebSocket)

### Target Component: `RemoteChatRepositoryImpl` & `PushNotificationManager`
- **Objective:** Upgrade local chat simulation into bidirectional real-time messaging between customers and shopkeepers.
- **Implementation Strategy:**
  - **Messaging Protocol:** Firebase Cloud Messaging (FCM) for low-latency wake-ups combined with WebSocket / Firestore Listeners during active chat sessions.
  - **End-to-End Privacy:** Keep message payloads encrypted with Signal Protocol or user-specific public keys before sending to cloud queues.
  - **Offline Outbox Pattern:** Chat messages typed while offline are written to Room with status `SENDING`, and auto-dispatched once network connectivity resumes.
- **Repository Interface:**
  - Implements `ChatRepository` (`getMessages(userId)`, `sendMessage(msg)`).
  - ViewModels continue consuming the same `ChatMessage` domain models without modification.

---

## 3. Paid Promotions & Featured Deals Engine

### Target Component: `PromotedDealsUseCase` & `DealStatsRepository`
- **Objective:** Allow merchants to boost their deals to the top of "Hot Deals Near You" and category listings for a specified time window.
- **Implementation Strategy:**
  - **Priority Scoring Formula:**
    $$\text{Score} = (\text{Discount \%} \times 0.4) + \left(\frac{1}{\text{Distance in km}} \times 0.3\right) + (\text{Boost Tier} \times 0.3)$$
  - High-tier boosted deals are rendered with an elegant golden "Featured" badge.
  - Real-time click/view impression tracking streamed to the local `AnalyticsTracker` and synchronized with the merchant dashboard.
- **Repository Interface:**
  - Extends `DealRepository.getDeals()` with optional `isPromoted` weighting.

---

## 4. Merchant Subscriptions & Monetization (Google Play Billing 6+)

### Target Component: `PlayBillingManager` & `SubscriptionRepositoryImpl`
- **Objective:** Monetize merchant accounts with tiered subscription plans (e.g., Free: up to 3 active deals; Pro: unlimited deals, live chat, verified badge, and analytics).
- **Implementation Strategy:**
  - Integrate official `com.android.billingclient:billing-ktx`.
  - Server-side purchase verification using Google Play Developer API (or Firebase Cloud Functions) to prevent client-side receipt spoofing.
  - Real-time entitlement state emitted via `StateFlow<MerchantSubscriptionTier>`.
- **Repository Interface:**
  - New `SubscriptionRepository` providing `isProMerchant(sellerId): Flow<Boolean>`.
  - Guarded in `AddEditDealScreen` when deal creation exceeds free tier limits.

---

## 5. Roadmap Timeline & Release Milestones

| Milestone | Version | Core Deliverables | Architecture Impact |
| :--- | :--- | :--- | :--- |
| **v1.0** *(Current)* | `1.0.0` | 100% offline, Room DB, local simulated chat/orders, encrypted session, R8 minified AAB. | Established repository interfaces and Clean Architecture. |
| **v1.1** | `1.1.0` | Dhaka neighborhood crowd-sourcing, export/import backup file, multi-image support. | DAO additions, Room schema migration v1 -> v2. |
| **v2.0** | `2.0.0` | Cloud sync (Firebase/Supabase), Live chat with FCM push notifications. | Swap in `SyncDealRepository` and `RemoteChatRepository`. |
| **v2.1** | `2.1.0` | Google Play Billing merchant subscriptions, promoted deals engine, advanced analytics. | Add `SubscriptionRepository` and Billing Client wrapper. |
