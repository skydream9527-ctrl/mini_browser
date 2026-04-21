package com.minibrowser.app.tab

import com.minibrowser.app.engine.GeckoEngineManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession

class TabManager(private val runtime: GeckoRuntime) {

    private val _tabs = MutableStateFlow<List<TabInfo>>(emptyList())
    val tabs: StateFlow<List<TabInfo>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    private val sessions = mutableMapOf<String, GeckoSession>()

    val tabCount: Int get() = _tabs.value.size
    val activeTab: TabInfo? get() = _tabs.value.find { it.id == _activeTabId.value }

    fun createTab(isIncognito: Boolean = false): TabInfo {
        val tab = TabInfo(isIncognito = isIncognito)
        val session = GeckoSession().apply { open(runtime) }
        sessions[tab.id] = session
        _tabs.value = _tabs.value + tab
        switchTo(tab.id)
        return tab
    }

    fun switchTo(tabId: String) {
        _activeTabId.value = tabId
        _tabs.value = _tabs.value.map { it.copy(isActive = it.id == tabId) }
    }

    fun closeTab(tabId: String) {
        sessions[tabId]?.close()
        sessions.remove(tabId)
        val remaining = _tabs.value.filter { it.id != tabId }
        _tabs.value = remaining

        if (_activeTabId.value == tabId) {
            val newActive = remaining.lastOrNull()
            _activeTabId.value = newActive?.id
            if (newActive != null) {
                _tabs.value = _tabs.value.map { it.copy(isActive = it.id == newActive.id) }
            }
        }
    }

    fun getSession(tabId: String): GeckoSession? = sessions[tabId]

    fun updateTab(tabId: String, title: String? = null, url: String? = null) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(
                    title = title ?: tab.title,
                    url = url ?: tab.url
                )
            } else tab
        }
    }

    fun closeAllIncognito() {
        val incognitoIds = _tabs.value.filter { it.isIncognito }.map { it.id }
        incognitoIds.forEach { id ->
            sessions[id]?.close()
            sessions.remove(id)
        }
        _tabs.value = _tabs.value.filter { !it.isIncognito }
        if (_activeTabId.value in incognitoIds) {
            val newActive = _tabs.value.lastOrNull()
            _activeTabId.value = newActive?.id
            if (newActive != null) {
                _tabs.value = _tabs.value.map { it.copy(isActive = it.id == newActive.id) }
            }
        }
    }

    fun closeAll() {
        sessions.values.forEach { it.close() }
        sessions.clear()
        _tabs.value = emptyList()
        _activeTabId.value = null
    }
}
