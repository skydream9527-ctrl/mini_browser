package com.minibrowser.app.adblocker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdBlocker {

    private val _blockedCount = MutableStateFlow(0)
    val blockedCount: StateFlow<Int> = _blockedCount.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun onAdBlocked(url: String, totalCount: Int) {
        _blockedCount.value = totalCount
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    fun resetCount() {
        _blockedCount.value = 0
    }
}
