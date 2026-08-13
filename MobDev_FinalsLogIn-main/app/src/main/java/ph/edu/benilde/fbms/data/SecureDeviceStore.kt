package ph.edu.benilde.fbms.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the Device Secret in Android Keystore-backed encrypted storage.
 *
 * Per the docs: "Store the Device Secret only in Android Keystore-backed secure
 * storage, never plain SharedPreferences, never logged, never shown on screen
 * after activation." There is also no "forgot my secret" endpoint — if this
 * storage is cleared, the tablet must be re-activated with a brand-new token.
 */
class SecureDeviceStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "fbms_device_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isActivated(): Boolean = prefs.contains(KEY_DEVICE_SECRET)

    fun saveActivation(deviceSecret: String, deviceCode: String, deviceName: String) {
        prefs.edit()
            .putString(KEY_DEVICE_SECRET, deviceSecret)
            .putString(KEY_DEVICE_CODE, deviceCode)
            .putString(KEY_DEVICE_NAME, deviceName)
            .apply()
    }

    fun getDeviceSecret(): String? = prefs.getString(KEY_DEVICE_SECRET, null)
    fun getDeviceCode(): String? = prefs.getString(KEY_DEVICE_CODE, null)
    fun getDeviceName(): String? = prefs.getString(KEY_DEVICE_NAME, null)

    /** Only for the "deactivated / needs re-activation" recovery path. */
    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_DEVICE_SECRET = "device_secret"
        const val KEY_DEVICE_CODE = "device_code"
        const val KEY_DEVICE_NAME = "device_name"
    }
}
