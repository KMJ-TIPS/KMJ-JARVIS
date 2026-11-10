package com.kmj.jarvis.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.kmj.jarvis.Branding

/**
 * The one-page permission gate: every essential runtime permission in a single
 * "Allow all" Android dialog, plus optional extras the user can grant with one tap
 * from their system pages. Continue unlocks when the essentials are in.
 */
@Composable
fun PermissionsStep(onContinue: () -> Unit) {
    val context = LocalContext.current
    var statuses by remember { mutableStateOf(readStatuses(context)) }

    val essentials = remember {
        buildList {
            add(android.Manifest.permission.RECORD_AUDIO)
            add(android.Manifest.permission.READ_CONTACTS)
            add(android.Manifest.permission.READ_CALENDAR)
            if (Build.VERSION.SDK_INT >= 33) add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val allowAll = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        statuses = readStatuses(context)
    }

    // Statuses can change while we're paused (user returns from a system page).
    LifecycleResumeEffect(Unit) { statuses = readStatuses(context); onPauseOrDispose { } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Text("Permissions", style = MaterialTheme.typography.displaySmall)
        Text(
            "${Branding.ASSISTANT_NAME} needs these to hear you and act for you. Everything stays on the phone unless you ask for Gemini.",
            style = MaterialTheme.typography.bodyLarge,
            color = Muted,
            modifier = Modifier.padding(top = 8.dp),
        )

        Section(null, Modifier.padding(top = 24.dp)) {
            PermRow("Microphone", "To talk and for the wake word", statuses.mic)
            PermRow("Notifications", "Reminders and answers", statuses.notifications)
            PermRow("Contacts", "Call or message people you name", statuses.contacts)
            PermRow("Calendar", "Read and create events", statuses.calendar)
        }

        val allEssential = statuses.mic && statuses.notifications && statuses.contacts && statuses.calendar
        Button(
            onClick = { allowAll.launch(essentials.toTypedArray()) },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp),
            enabled = !allEssential,
        ) { Text(if (allEssential) "All allowed" else "Allow all") }

        Text("Optional extras", style = MaterialTheme.typography.titleSmall, color = Muted, modifier = Modifier.padding(top = 28.dp, start = 4.dp))
        Section(null, Modifier.padding(top = 8.dp)) {
            OptionalRow("Exact alarms", "Reminders ring on time") {
                if (Build.VERSION.SDK_INT >= 31) {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                }
            }
            OptionalRow("Draw over apps", "Floating orb over any screen") {
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
            }
            OptionalRow("Phone control", "Let it read the screen and tap") {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp),
            enabled = statuses.mic,
        ) { Text("Continue") }
        TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("Skip for now")
        }
        Spacer(Modifier.height(24.dp))
    }
}

private data class Statuses(val mic: Boolean, val notifications: Boolean, val contacts: Boolean, val calendar: Boolean)

private fun readStatuses(context: Context): Statuses {
    fun ok(p: String) = ContextCompat.checkSelfPermission(context, p) == android.content.pm.PackageManager.PERMISSION_GRANTED
    return Statuses(
        mic = ok(android.Manifest.permission.RECORD_AUDIO),
        // Before API 33 there is no runtime notification permission: treat as allowed.
        notifications = Build.VERSION.SDK_INT < 33 || ok(android.Manifest.permission.POST_NOTIFICATIONS),
        contacts = ok(android.Manifest.permission.READ_CONTACTS),
        calendar = ok(android.Manifest.permission.READ_CALENDAR),
    )
}

@Composable
private fun PermRow(title: String, detail: String, allowed: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(if (allowed) Color(0xFF4ADE80) else MaterialTheme.colorScheme.outlineVariant, CircleShape),
        )
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        if (allowed) {
            Text("Allowed", color = Accent, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun OptionalRow(title: String, detail: String, onOpen: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        TextButton(onClick = onOpen) { Text("Allow") }
    }
}
