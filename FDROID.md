# F-Droid Inclusion Guide for Senswear

Senswear is 100% Free and Open-Source Software (FLOSS) licensed under Apache-2.0, completely free of proprietary tracking libraries, closed binary blobs, or cloud telemetry.

---

## 📋 Package & Repository Details

* **Package ID**: `com.senswear.app`
* **Application Name**: Senswear
* **License**: Apache-2.0
* **Source Repository**: `https://github.com/krtvysinghh/Senswear`
* **Issue Tracker**: `https://github.com/krtvysinghh/Senswear/issues`
* **Build Tool**: Gradle with Android Gradle Plugin 8.9.0 & Kotlin 2.1.20
* **Minimum SDK**: 26 (Android 8.0)
* **Target SDK**: 36 (Android 16)

---

## 📦 Metadata Locations

1. **F-Droid Build Recipe**: [`fdroid/com.senswear.app.yml`](fdroid/com.senswear.app.yml) & [`.fdroid.yml`](.fdroid.yml)
2. **Fastlane Metadata Directory**: [`fastlane/metadata/android/en-US/`](fastlane/metadata/android/en-US/)
   * `title.txt`
   * `short_description.txt`
   * `full_description.txt`
   * `images/icon.png`
   * `changelogs/2.txt`

---

## 🛠️ How to Submit to F-Droid Official Repository (`fdroiddata`)

1. **Fork the Official `fdroiddata` Repository**:
   Visit [gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata) and create a fork.

2. **Add the Metadata File**:
   Copy [`fdroid/com.senswear.app.yml`](fdroid/com.senswear.app.yml) into the `metadata/` directory of your `fdroiddata` clone:
   ```bash
   cp fdroid/com.senswear.app.yml /path/to/fdroiddata/metadata/com.senswear.app.yml
   ```

3. **Verify Build via F-Droid Server / Docker**:
   ```bash
   fdroid checkupdates com.senswear.app
   fdroid lint com.senswear.app
   fdroid build com.senswear.app
   ```

4. **Create a Merge Request**:
   Submit a Merge Request to [gitlab.com/fdroid/fdroiddata/-/merge_requests](https://gitlab.com/fdroid/fdroiddata/-/merge_requests).

---

## 🛡️ Anti-Features Review

Senswear has **NO Anti-Features**:
* :white_check_mark: **No Ads**: Zero advertising SDKs.
* :white_check_mark: **No Tracking**: Zero telemetry, zero analytics, zero crash-reporting SDKs.
* :white_check_mark: **No Non-Free Network Services**: Direct BLE communication is local; optional OAuth2 cloud plugins are disabled by default and user-configured.
* :white_check_mark: **No Non-Free Dependencies**: Pure Kotlin/Compose/AndroidX dependencies built from source.
