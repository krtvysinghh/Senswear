# Official F-Droid & IzzyOnDroid Submission Manifest

This document contains pre-validated, copy-paste-ready submission data to list Senswear in the F-Droid ecosystem.

---

## 1. Official F-Droid Merge Request (Main Index)

* **Repository**: [gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata)
* **Action**: Create a new file in your `fdroiddata` fork at `metadata/com.senswear.app.yml`
* **Contents**: Copy entire content from [`fdroid/com.senswear.app.yml`](../fdroid/com.senswear.app.yml)

### Pre-Filled Merge Request Title & Description:
**Title**: `New App: Senswear (com.senswear.app)`

**Description**:
```markdown
### Application Summary
- **App Name**: Senswear
- **Package ID**: `com.senswear.app`
- **License**: Apache-2.0
- **Source Code**: https://github.com/krtvysinghh/Senswear
- **Issue Tracker**: https://github.com/krtvysinghh/Senswear/issues
- **Summary**: Privacy-first Universal Wearable & Biometrics Platform for Android.

### Compliance Checklist
- [x] The upstream repository is public and licensed under a free software license.
- [x] No proprietary tracking, advertising, or binary blobs are included.
- [x] The app builds from source via `./gradlew assembleRelease`.
- [x] Fastlane metadata and icon are located in `fastlane/metadata/android/en-US/`.
```

---

## 2. IzzyOnDroid Inclusion Request (Direct Client Distribution)

* **Issue Tracker**: [gitlab.com/IzzyOnDroid/repo/-/issues/new?issuable_template=inclusion](https://gitlab.com/IzzyOnDroid/repo/-/issues/new?issuable_template=inclusion)

### Pre-Filled Inclusion Request:
**Title**: `[Inclusion] Senswear (com.senswear.app)`

**Description**:
```markdown
* **App Name**: Senswear
* **Package ID**: com.senswear.app
* **Source Code**: https://github.com/krtvysinghh/Senswear
* **License**: Apache-2.0
* **Release Artifacts**: https://github.com/krtvysinghh/Senswear/releases
* **Description**: Privacy-first Universal Wearable & Biometrics Platform for Android.
* **Anti-Features**: None (Zero telemetry, zero ads, 100% on-device SQLite storage).
* **Fastlane Structure**: Present at `fastlane/metadata/android/en-US/`.
```
