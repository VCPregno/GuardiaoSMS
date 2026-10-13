package com.tcc.guardiaosms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.tcc.guardiaosms.data.preferences.ThemeMode
import com.tcc.guardiaosms.ui.navigation.GuardiaoSmsNavGraph
import com.tcc.guardiaosms.ui.theme.GuardiaoSmsTheme

class MainActivity : ComponentActivity() {

    private val requestSmsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasSmsPermissionState.value = granted
        }
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* opcional */ }

    private var hasSmsPermissionState = mutableStateOf(false)

    /** Mensagem a abrir no detalhe, vinda do toque na notificação de alerta. */
    private val openRecordIdState = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasSmsPermissionState.value = isPermissionGranted(Manifest.permission.RECEIVE_SMS)

        // Só no primeiro onCreate: ao girar a tela, o intent original é entregue
        // de novo e reabriria o detalhe por cima do que o usuário estava vendo.
        if (savedInstanceState == null) handleOpenRecordIntent(intent)

        // POST_NOTIFICATIONS só existe a partir do Android 13 (API 33);
        // em versões anteriores a notificação funciona sem essa permissão explícita.
        if (Build.VERSION.SDK_INT >= 33 &&
            !isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val container = (application as GuardiaoSmsApplication).container

        setContent {
            val hasSmsPermission by hasSmsPermissionState
            val openRecordId by openRecordIdState
            val themeMode by container.preferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = themeMode.isDark(isSystemInDarkTheme())

            // Barras de sistema (status e navegação) seguem o tema DO APP, não só o do celular.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            GuardiaoSmsTheme(darkTheme = darkTheme) {
                GuardiaoSmsNavGraph(
                    container = container,
                    hasSmsPermission = hasSmsPermission,
                    onRequestPermission = { requestSmsPermission.launch(Manifest.permission.RECEIVE_SMS) },
                    openRecordId = openRecordId,
                    onOpenRecordConsumed = { openRecordIdState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // App já aberto (launchMode singleTop): a notificação chega por aqui.
        setIntent(intent)
        handleOpenRecordIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Cobre o caso do usuário conceder a permissão pelas Configurações do
        // sistema e voltar ao app (fora do fluxo do diálogo de permissão).
        hasSmsPermissionState.value = isPermissionGranted(Manifest.permission.RECEIVE_SMS)
    }

    private fun handleOpenRecordIntent(intent: Intent?) {
        val recordId = intent?.getLongExtra(EXTRA_RECORD_ID, -1L) ?: -1L
        if (recordId > 0) openRecordIdState.value = recordId
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        /** Extra com o id (Room) da mensagem a abrir no detalhe. */
        const val EXTRA_RECORD_ID = "com.tcc.guardiaosms.extra.RECORD_ID"
    }
}
