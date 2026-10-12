# Firebase Free Cloud Setup Guide ($0 Cost) / ফায়ারবেস ক্লাউড সেটআপ গাইড

> **Local Deals Finder** utilizes an **Offline-First** hybrid architecture. 
> The app works 100% offline out-of-the-box with Room SQLite local database. 
> When connected to Google Firebase (100% Free Spark Plan), the app unlocks **real-time cloud deal sync, live seller order updates, real-time customer chat, and FCM push notifications**.

---

## 🇧🇩 বাংলায় সহজ গাইড (Bengali Step-by-Step Guide)

### ১. ফায়ারবেসে সম্পূর্ণ বিনামূল্যে প্রজেক্ট তৈরি (১০০% ফ্রি স্পার্ক প্ল্যান):
1. ব্রাউজারে যান: [https://console.firebase.google.com](https://console.firebase.google.com)
2. আপনার গুগল (Gmail) অ্যাকাউন্ট দিয়ে লগইন করুন।
3. **"Add project"** বা **"Create a project"** বাটনে ক্লিক করুন।
4. প্রজেক্টের নাম দিন (যেমন: `Local Deals Finder`) এবং Continue চাপুন।
5. Google Analytics অপশনটি বন্ধ (Disable) বা চালু রাখতে পারেন (আপনার ইচ্ছা), তারপর **Create project** চাপুন।

### ২. অ্যান্ড্রয়েড অ্যাপ যুক্ত করুন:
1. প্রজেক্ট ড্যাশবোর্ডে **Android** আইকনে ক্লিক করুন।
2. **Android package name** এর ঘরে লিখুন:
   ```
   com.localdeals.finder
   ```
3. App nickname দিতে পারেন (যেমন: `Local Deals Finder`)।
4. **Register app** বাটনে ক্লিক করুন।

### ৩. `google-services.json` ফাইলটি ডাউনলোড ও প্রজেক্টে বসানো:
1. স্ক্রিনে **Download google-services.json** বাটন পাবেন, ফাইলটি ডাউনলোড করুন।
2. আপনার কম্পিউটারের এই ফোল্ডারে ফাইলটি কপি করে পেস্ট করে দিন (বিদ্যমান ফাইল রিপ্লেস করুন):
   ```
   e:\Local Deals Finder\app\google-services.json
   ```

### ৪. ফায়ারবেস কনসোলে ক্লাউড সার্ভিস চালু করা:
ফায়ারবেস কনসোলের বাম পাশের মেনু থেকে নিচের দুটি ফিচার ১ ক্লিকে সক্রিয় (Enable) করুন:

1. **Firestore Database (ফ্রি ডিল ও অর্ডার সিঙ্ক):**
   - বাম মেনু থেকে **Build > Firestore Database**-এ যান।
   - **Create database** বাটনে ক্লিক করুন।
   - Database location সিলেক্ট করুন (যেমন: `asia-south1 (Mumbai)` বাংলাদেশের জন্য সবচেয়ে দ্রুত)।
   - Security Rules হিসেবে **Start in test mode** সিলেক্ট করে Done দিন।
2. **Firebase Cloud Messaging (FCM পুশ নোটিফিকেশন):**
   - বাম মেনু থেকে **Project settings > Cloud Messaging** দেখুন। এটি স্বয়ংক্রিয়ভাবে সক্রিয় থাকে। কোনো অতিরিক্ত কনফিগারেশন লাগবে না।

---

## 🇺🇸 English Architecture & Technical Overview

### 1. Offline-First Guarantee
- **Local Database (Room SQLite):** Remains the single source of truth for the Jetpack Compose UI. Instant 0ms latency rendering on app launch.
- **`FirebaseSyncManager`:** Runs non-blocking coroutines in the background (`Dispatchers.IO`).
- **Zero Crashes:** If the device is offline or no Firebase credentials are configured, the app gracefully catches all exceptions and continues full offline operation.

### 2. Firestore Schema
- **`deals` Collection:**
  - `id` (String): Unique deal ID.
  - `title`, `description`, `originalPrice`, `discountPrice`, `discountPercentage`
  - `category`, `sellerId`, `storeName`, `latitude`, `longitude`, `imageUrl`, `validUntil`
- **`orders` Collection:**
  - `id` (String): Unique order ID.
  - `dealId`, `userId`, `sellerId`, `status` (`PENDING`, `ACCEPTED`, `READY`, `COMPLETED`, `CANCELLED`)
  - `quantity`, `totalPrice`, `timestamp`
- **`chats` Collection:**
  - Subcollections organized by conversation or seller-buyer pair with real-time snapshot listeners.

### 3. Build & Run
```bash
# Build & Run Debug
./gradlew installDebug

# Build Production AAB
./gradlew bundleRelease
```
