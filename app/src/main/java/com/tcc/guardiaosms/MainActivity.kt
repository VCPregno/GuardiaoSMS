package com.tcc.guardiaosms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasSmsPermissionState.value = isPermissionGranted(Manifest.permission.RECEIVE_SMS)

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
            GuardiaoSmsTheme {
                GuardiaoSmsNavGraph(
                    container = container,
                    hasSmsPermission = hasSmsPermission,
                    onRequestPermission = { requestSmsPermission.launch(Manifest.permission.RECEIVE_SMS) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Cobre o caso do usuário conceder a permissão pelas Configurações do
        // sistema e voltar ao app (fora do fluxo do diálogo de permissão).
        hasSmsPermissionState.value = isPermissionGranted(Manifest.permission.RECEIVE_SMS)
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
