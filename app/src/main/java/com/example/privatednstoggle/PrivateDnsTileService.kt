package com.example.privatednstoggle

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

/**
 * Quick Settings tile that toggles Android's "Private DNS" (DNS-over-TLS) mode
 * between "off" and "hostname" (pointed at DNS_HOST) with a single tap.
 *
 * Silent toggling requires the WRITE_SECURE_SETTINGS permission, which cannot
 * be granted through the normal runtime permission dialog. Grant it once via
 * ADB (see README.md):
 *
 *   adb shell pm grant com.example.privatednstoggle android.permission.WRITE_SECURE_SETTINGS
 *
 * Without that grant, tapping the tile falls back to opening the system
 * Private DNS settings screen so the user can flip it manually.
 */
class PrivateDnsTileService : TileService() {

    companion object {
        // Change this to whatever private DNS provider you want.
        // Examples: "dns.google", "one.one.one.one", "dns.adguard.com"
        const val DNS_HOST = "dns.google"

        private const val MODE_KEY = "private_dns_mode"
        private const val SPECIFIER_KEY = "private_dns_specifier"
        private const val MODE_OFF = "off"
        private const val MODE_ON = "hostname"
    }

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()

        if (!hasWriteSecureSettings()) {
            // Can't toggle silently — open the settings screen instead.
            Toast.makeText(this, "Grant WRITE_SECURE_SETTINGS via ADB to toggle instantly. Opening settings…", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_PRIVATE_DNS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapse(intent)
            return
        }

        val currentlyOn = isPrivateDnsOn()
        if (currentlyOn) {
            Settings.Global.putString(contentResolver, MODE_KEY, MODE_OFF)
        } else {
            Settings.Global.putString(contentResolver, MODE_KEY, MODE_ON)
            Settings.Global.putString(contentResolver, SPECIFIER_KEY, DNS_HOST)
        }
        refreshTile()
    }

    private fun isPrivateDnsOn(): Boolean {
        val mode = Settings.Global.getString(contentResolver, MODE_KEY)
        return mode == MODE_ON
    }

    private fun hasWriteSecureSettings(): Boolean {
        return checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val isOn = isPrivateDnsOn()
        tile.state = if (isOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isOn) "DNS: $DNS_HOST" else "Private DNS Off"
        tile.updateTile()
    }
}
