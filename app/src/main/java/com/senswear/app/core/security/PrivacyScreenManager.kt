package com.senswear.app.core.security

import android.app.Activity
import android.view.WindowManager

/**
 * Toggles WindowManager.LayoutParams.FLAG_SECURE to prevent sensitive health records
 * from being captured in screenshots or exposed in Android's Recent Apps task switcher.
 */
class PrivacyScreenManager {

    fun enablePrivacyProtection(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun disablePrivacyProtection(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
