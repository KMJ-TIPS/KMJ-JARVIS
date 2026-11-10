package com.kmj.jarvis

import android.Manifest
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.kmj.jarvis.assistant.AssistantViewModel
import com.kmj.jarvis.assistant.VoiceService
import com.kmj.jarvis.assistant.VoiceSession
import com.kmj.jarvis.orb.FloatingOrb
import com.kmj.jarvis.ui.KMJApp

class MainActivity : ComponentActivity() {
    private val assistant: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { KMJApp(assistant) }
        if (savedInstanceState == null) listenIfAsked(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        listenIfAsked(intent)
    }

    override fun onStart() {
        super.onStart()
        VoiceSession.get(this).screenVisible = true
        FloatingOrb.appVisible = true
        FloatingOrb.sync(this)
        // After a reboot or a kill, the wake-word listener comes back when the app is opened.
        if (VoiceService.wakeWordWanted(this)) VoiceService.start(this)
    }

    // Leaving the screen no longer stops listening: VoiceService keeps the session going.
    override fun onStop() {
        super.onStop()
        VoiceSession.get(this).screenVisible = false
        FloatingOrb.appVisible = false
        FloatingOrb.sync(this)
    }

    /**
     * Opened as the phone's digital assistant (long-press / corner swipe): start listening
     * straight away, as long as the mic is already allowed and a key is saved.
     */
    private fun listenIfAsked(intent: Intent?) {
        if (intent?.action != Intent.ACTION_ASSIST) return
        val micAllowed = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        Diag.i("session: opened as assistant (mic allowed=$micAllowed)")
        if (!micAllowed || !assistant.state.value.hasKey) return
        // Never listen on a locked phone: whoever holds it could use the assistant.
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (!keyguard.isKeyguardLocked) {
            assistant.start()
            return
        }
        Diag.i("session: phone locked, asking to unlock first")
        keyguard.requestDismissKeyguard(
            this,
            object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() = assistant.start()
            },
        )
    }
}
