package com.example

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.edit
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import com.example.ui.DashboardScreen
import com.example.ui.GuardianViewModel
import com.example.ui.theme.CostGuardianTheme
import com.google.firebase.FirebaseApp

private const val TAG = "CostGuardianActivity"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

class MainActivity : ComponentActivity() {

    private companion object {
        const val GCP_COMPUTE_READ_ONLY_SCOPE = "https://www.googleapis.com/auth/compute.readonly"
        const val STATE_PENDING_GCP_PROJECT = "pending_gcp_project_id"
    }

    private val viewModel: GuardianViewModel by viewModels()
    private var pendingGcpProjectId: String? = null

    private val gcpAuthorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val projectId = pendingGcpProjectId.also { pendingGcpProjectId = null }
        if (result.resultCode != RESULT_OK || projectId == null || result.data == null) {
            viewModel.showToast("Google Cloud authorization was cancelled.")
            return@registerForActivityResult
        }
        runCatching {
            Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(result.data!!)
        }.onSuccess { authorization ->
            val token = authorization.accessToken
            if (token.isNullOrBlank()) {
                viewModel.showToast("Google did not grant Compute Engine read-only access.")
            } else {
                viewModel.startLiveGcpScan(projectId, token)
            }
        }.onFailure { error ->
            Log.w(TAG, "GCP authorization result could not be read", error)
            viewModel.showToast("Could not complete Google Cloud authorization. Please try again.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingGcpProjectId = savedInstanceState?.getString(STATE_PENDING_GCP_PROJECT)

        // Configure Firebase App Check with launch intent before any Firebase operations
        configureAppCheck(this, intent)

        // Keep system controls readable over the dark, edge-to-edge dashboard.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            CostGuardianTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    onAuthorizeGcp = ::authorizeGcpReadOnly,
                    onDisconnectGcp = ::disconnectGcpAccess
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_PENDING_GCP_PROJECT, pendingGcpProjectId)
        super.onSaveInstanceState(outState)
    }

    private fun authorizeGcpReadOnly(projectId: String) {
        pendingGcpProjectId = projectId
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(GCP_COMPUTE_READ_ONLY_SCOPE)))
            .build()
        Identity.getAuthorizationClient(this)
            .authorize(request)
            .addOnSuccessListener { authorization ->
                if (authorization.hasResolution()) {
                    val pendingIntent = authorization.pendingIntent
                    if (pendingIntent == null) {
                        pendingGcpProjectId = null
                        viewModel.showToast("Google authorization could not be opened. Please try again.")
                        return@addOnSuccessListener
                    }
                    try {
                        gcpAuthorizationLauncher.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                        )
                    } catch (error: IntentSender.SendIntentException) {
                        pendingGcpProjectId = null
                        Log.w(TAG, "Unable to launch Google authorization", error)
                        viewModel.showToast("Google authorization could not be opened. Please try again.")
                    }
                } else {
                    pendingGcpProjectId = null
                    val token = authorization.accessToken
                    if (token.isNullOrBlank()) {
                        viewModel.showToast("Google did not grant Compute Engine read-only access.")
                    } else {
                        viewModel.startLiveGcpScan(projectId, token)
                    }
                }
            }
            .addOnFailureListener { error ->
                pendingGcpProjectId = null
                Log.w(TAG, "GCP read-only authorization failed", error)
                viewModel.showToast("Google Cloud authorization failed. Check Google Play services and try again.")
            }
    }

    private fun disconnectGcpAccess() {
        Identity.getAuthorizationClient(this)
            .revokeAccess(RevokeAccessRequest.builder().build())
            .addOnSuccessListener {
                viewModel.resetCurrentProject()
                viewModel.showToast("Google Cloud authorization disconnected.")
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "GCP authorization could not be revoked", error)
                viewModel.showToast("Could not disconnect Google Cloud access. Try again.")
            }
    }

    private fun configureAppCheck(context: Context, intent: Intent?) {
        try {
            val firebaseApp = FirebaseApp.getApps(context).firstOrNull()
                ?: FirebaseApp.initializeApp(context)
            if (firebaseApp == null) {
                Log.i(TAG, "Firebase is not configured; offline guidance remains available.")
                return
            }
            if (BuildConfig.DEBUG) {
                val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
                if (!debugToken.isNullOrEmpty()) {
                    val persistenceKey = firebaseApp.persistenceKey
                    val prefs = context.getSharedPreferences(
                        "$DEBUG_STORE_PREFIX$persistenceKey",
                        Context.MODE_PRIVATE
                    )
                    prefs.edit(commit = true) { putString(DEBUG_SECRET_KEY, debugToken) }
                    Log.d(TAG, "App Check debug token stored successfully.")
                }
            }
            FirebaseAppCheckProviderInstaller.install()
        } catch (e: Exception) {
            Log.e(TAG, "App Check initialization bypassed (running in offline/local mode): ${e.message}")
        }
    }
}
