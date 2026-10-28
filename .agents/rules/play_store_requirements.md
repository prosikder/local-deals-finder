# Google Play Store SDK & Release Requirements

All Android applications developed or released for this user must adhere to the following Google Play requirements:

1. **Target SDK & Compile SDK:**
   - Always set `compileSdk = 36` and `targetSdk = 36` (or higher as mandated by Google Play Console).
   - Never use `targetSdk 34` or lower for new apps, as Google Play Console will reject the submission with:
     *"Your app currently targets API level X and must target at least API level 36"*.

2. **App Bundle & Release Versioning:**
   - Always build release artifacts as signed Android App Bundles (`.aab`) via `./gradlew bundleRelease`.
   - Ensure `versionCode` is incremented on every release update (e.g. 1 -> 2 -> 3).

3. **NDK Debug Symbols:**
   - Include debug symbols in the release build block in `app/build.gradle.kts` to avoid Play Console warnings:
     ```kotlin
     buildTypes {
         getByName("release") {
             ndk {
                 debugSymbolLevel = "FULL"
             }
         }
     }
     ```

4. **Testing Progression:**
   - **Internal Testing**: Immediate 5-minute sanity check for developer devices.
   - **Closed Testing (App Hive)**: 20 testers for 14 continuous days for personal accounts before applying for Production.
