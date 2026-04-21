package com.minibrowser.app.tab

data class TabInfo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "新标签页",
    val url: String = "",
    val isIncognito: Boolean = false,
    val isActive: Boolean = false
)
