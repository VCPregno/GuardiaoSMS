package com.tcc.guardiaosms.data.preferences

/** Preferência de tema do app. [SYSTEM] (padrão) segue o modo claro/escuro do celular. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemIsDark
        LIGHT -> false
        DARK -> true
    }
}
