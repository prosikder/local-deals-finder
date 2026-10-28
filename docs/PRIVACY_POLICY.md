# Privacy Policy for Local Deals Finder

**Effective Date:** October 2026  
**Application:** Local Deals Finder (`com.localdeals.finder`)  
**Developer:** Pro LAB / Prokash Sikder  
**Contact:** support@localdeals.finder | [GitHub Repository](https://github.com/prosikder/local-deals-finder)

---

### 1. Overview
Local Deals Finder ("we", "our", or "the app") is a privacy-first Android application designed to help users discover local discounts, dining offers, grocery deals, and neighborhood promotions in Dhaka.

We are strictly committed to user privacy. **Local Deals Finder operates 100% offline-first by default with zero invasive tracking, zero third-party advertising frameworks, and zero unauthorized data harvesting.**

---

### 2. Information We Handle and How It Is Used

#### A. Device Location (GPS & Network Location)
- **Permissions:** `android.permission.ACCESS_FINE_LOCATION` and `android.permission.ACCESS_COARSE_LOCATION`.
- **Purpose:** To calculate real-time straight-line (Haversine) distances to nearby merchants and sort deals by physical proximity.
- **On-Device Only:** All location coordinates and distance sorting calculations are performed **strictly in-memory on your device**.
- **No Cloud Transmission:** Your location coordinates are **never** transmitted, stored on remote servers, shared with advertisers, or logged to external analytics platforms.
- **Manual Fallback:** If you deny location permission, you can choose a predefined neighborhood in Dhaka (e.g., Dhanmondi, Gulshan, Uttara, Mirpur) manually.

#### B. Notifications
- **Permission:** `android.permission.POST_NOTIFICATIONS` (Android 13+ / API 33+).
- **Purpose:** Delivers local alerts when a wishlisted deal is expiring or when an order status updates.
- **Privacy:** Notifications are scheduled locally via Android WorkManager. No notification tracking data leaves your device.

#### C. Local Application Data & Storage
- All user profiles, wishlists, order histories, and local simulated chat messages are stored locally on your device in an SQLite database (Room) and encrypted Android DataStore using Android KeyStore cryptography (AES-GCM).
- You can completely wipe all locally saved data at any time from **Profile > Reset Demo Data / Clear All**.

---

### 3. Third-Party SDKs and Data Sharing
- **No Advertising:** We do not display external banner ads, interstitial ads, or video ads.
- **No Third-Party Analytics:** We do not use Google Firebase Analytics, Facebook SDK, AppsFlyer, or any third-party behavioral trackers.
- **No Data Sharing:** We do not sell, rent, or disclose personal user data to any third party.

---

### 4. Children’s Privacy
Local Deals Finder does not knowingly collect personal identifiable information from children under the age of 13.

---

### 5. Changes to This Privacy Policy
We may update this policy periodically to reflect future features. Any changes will be posted to this document with an updated effective date.

---

### 6. Contact Us
If you have any questions or feedback regarding this Privacy Policy, please contact:
- **Email:** support@localdeals.finder
- **Repository:** https://github.com/prosikder/local-deals-finder
