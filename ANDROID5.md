# Android 5.1 (API 22) port

This branch lets the Clean Your TV launcher install and run on Android 5.1 boxes
(tested on an Amlogic M8S+, Android 5.1.1). Upstream needs Android 8 (API 26):
<https://github.com/Gathin23/cleanyourtv>.

## What changed (launcher only)

- `minSdk` 26 -> 22, with core library desugaring for `java.time`, `java.util.function` and `java.nio.file`.
- `Compat.java`: every platform call newer than API 22 goes through it, with the nearest older behaviour as fallback.
- `PillTextView.java`: the top-bar pills tint their own icons, since `android:drawableTint` is Android 6.

## Known differences on Android 5.1

- No direct boot, no hardware bitmaps, no package change sequence (package broadcasts are used instead).
- "Online" means a connected network; Android 5 cannot tell whether the internet was validated.
- TV inputs cannot be hidden or renamed (Android 7 APIs).
- The repository's install scripts use `cmd package`, which Android 5 lacks. Install by hand:

```bash
cd launcher && ./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

Only the launcher was ported and tested; the debloat tools and add-ons are untouched.
