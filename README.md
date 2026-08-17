# Private DNS Toggle

A Quick Settings tile (the same shelf as your Wi-Fi/Bluetooth toggles) that
switches Android's built-in **Private DNS** setting on and off with one tap.

- **Off → tap →** Private DNS mode set to `hostname`, pointed at `dns.google`
  (edit `DNS_HOST` in `PrivateDnsTileService.kt` to use a different provider).
- **On → tap →** Private DNS mode set back to `off`.

## Why this needs one manual setup step

Android does not let a normal app silently change system DNS settings, even
with your permission at runtime — it requires the `WRITE_SECURE_SETTINGS`
permission, which Google restricts to system apps. There's no way around this
without rooting the device or being a device owner / MDM-managed profile. The
standard workaround (used by many tile utilities on the Play Store) is to
grant that one permission via ADB, once, from a computer.

**Without this step**, the tile still works — but tapping it just opens the
system "Private DNS" settings screen instead of silently flipping the switch.

## Setup

1. **Open in Android Studio**: File → Open → select the `PrivateDnsToggle` folder.
   Let Gradle sync (Android Studio will download the wrapper automatically).
2. **Build & install** on your phone (USB debugging enabled, or via a signed APK).
3. **Enable USB debugging** on the phone: Settings → About phone → tap "Build
   number" 7 times → Developer options → enable USB debugging.
4. **Connect the phone to a computer** with `adb` installed, then run:
   ```
   adb shell pm grant com.example.privatednstoggle android.permission.WRITE_SECURE_SETTINGS
   ```
   (The app's home screen has a button to copy this command for you.)
5. **Add the tile**: pull down the notification shade twice → tap the pencil/edit
   icon → drag "Private DNS" into your active tiles.

That's it — the tile now behaves like a native toggle.

## Notes

- Minimum SDK is 29 (Android 10); Private DNS itself exists from Android 9,
  but Quick Settings tile APIs used here target 10+ for reliability.
- If you ever uninstall and reinstall the app, you'll need to re-run the ADB
  grant command (permission grants don't survive an uninstall).
- Change `DNS_HOST` in `PrivateDnsTileService.kt` to switch providers, e.g.:
  - `dns.google` (Google)
  - `one.one.one.one` (Cloudflare)
  - `dns.adguard.com` (AdGuard, blocks ads/trackers)
