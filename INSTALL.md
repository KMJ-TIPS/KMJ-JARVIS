# Installing JARVIS

JARVIS isn't on the Play Store, so it's installed from the APK file.

1. **Download** `JARVIS-<version>-Android.apk` from the [Releases](../../releases) page,
   on your phone.
2. *(Optional, recommended)* **Check the file.** The release notes list its SHA-256; it must
   match. Every release is signed with the same key, whose SHA-256 fingerprint is
   `01:4C:15:BC:4A:2C:E2:05:FC:F0:35:35:BB:08:70:8D:78:BA:89:E4:C6:1B:FD:16:E5:83:F2:A0:7A:F2:5C:3B`.
   Android refuses to update JARVIS with a file signed by any other key.
3. **Open the APK.** Android asks to allow installing from your browser or Files app
   ("Install unknown apps"): allow it for that app, then tap **Install**.
4. **Open JARVIS** and follow the setup: paste your free Gemini API key from
   [Google AI Studio](https://aistudio.google.com/apikey) and allow the microphone.

## Optional features

- **Phone control** (use apps for you): JARVIS → Settings → Phone control. On Android 13+,
  Android first blocks this for installed-from-file apps: open **Settings → Apps → JARVIS →
  ⋮ (top right) → Allow restricted settings**, then turn on JARVIS under
  **Settings → Accessibility → Installed apps**.
- **What did I miss? / watch my messages:** JARVIS → Settings → Notifications → Turn on.
- **Floating orb:** JARVIS → Settings → Floating orb → allow "Display over other apps".
- **Keep it running** on Samsung, Xiaomi, Oppo, Vivo: allow JARVIS under Battery → Unrestricted.

## Updating

Download the newer APK and open it: it installs over the old one and keeps your settings.

## Uninstalling

Settings → Apps → JARVIS → Uninstall. This deletes everything JARVIS stored.
