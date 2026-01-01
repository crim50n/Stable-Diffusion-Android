package dev.minios.pdaiv1.presentation.screen.debug

import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.presentation.utils.Constants

class DebugMenuAccessor(
    private val preferenceManager: PreferenceManager,
) {

    private var tapCount = 0;

    operator fun invoke(): Boolean {
        if (preferenceManager.developerMode) {
            return true
        }
        tapCount++
        if (tapCount >= Constants.DEBUG_MENU_ACCESS_TAPS) {
            tapCount = 0
            preferenceManager.developerMode = true
            return true
        }
        return false
    }
}
