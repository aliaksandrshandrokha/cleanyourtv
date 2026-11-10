# Android 5.1 (API 22) port

This branch lets the Clean Your TV launcher install and run on Android 5.1 boxes
(tested on an Amlogic M8S+, Android 5.1.1). Upstream needs Android 8 (API 26):
<https://github.com/Gathin23/cleanyourtv>.

## What changed (launcher only)

- `minSdk` 26 -> 22, with core library desugaring for `java.time`, `java.util.function` and `java.nio.file`.
- `app_library` and `library_button` are on by default: Android 5 boxes ship ordinary (non-leanback) apps, which only the App library can list.
- `Compat.java`: every platform call newer than API 22 goes through it, with the nearest older behaviour as fallback.
- `PillTextView.java`: the top-bar pills tint their own icons, since `android:drawableTint` is Android 6.

## Layout defaults in this branch

Boxes of this era report 1080p at 240 dpi, so the screen is 1280 dp wide, not the 960 dp the layout was drawn for.
These defaults (all in `launcher/app/src/main/res/values/config.xml`) suit that screen; set them back to taste:

- `columns` 5, `dock_even_padding` on, and tiles scale up with the grid width instead of stopping at 180 dp.
- `single_row` and `dock_band_all` on, `show_library_pill` off: Home is one dock that scrolls sideways; the App library opens from the top bar.
- `icon_only_buttons` on (the Settings pill is just its icon), the header sits lower, and the single-row dock is lifted off the bottom edge.
- `icon_tiles` lists `org.mozilla.firefox`: a phone app with no banner, drawn as its icon on a plain tile.

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
