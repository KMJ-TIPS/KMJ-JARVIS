package com.kmj.jarvis.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kmj.jarvis.Branding
import com.kmj.jarvis.R
import com.kmj.jarvis.assistant.AssistantViewModel
import com.kmj.jarvis.wake.WakeWordModel

private enum class Step { Welcome, Permissions, Key, LocalCloud, Done }

/**
 * First run: welcome, the one-page permission gate, the user's Gemini key (required),
 * what runs where, done. All in five steps with a progress bar.
 */
@Composable
fun Onboarding(assistant: AssistantViewModel, hasKey: Boolean, onFinished: () -> Unit) {
    // Coming back for a new key only: skip the introduction.
    var index by rememberSaveable { mutableIntStateOf(if (assistant.onboarded) Step.Key.ordinal else 0) }
    val step = Step.entries[index]
    val next: () -> Unit = { if (index < Step.entries.lastIndex) index++ else onFinished() }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Progress(index, Modifier.padding(top = 20.dp, bottom = 8.dp))
        AnimatedContent(step, label = "step", modifier = Modifier.weight(1f)) { shown ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 24.dp)) {
                when (shown) {
                    Step.Welcome -> Page(Branding.ASSISTANT_NAME, "A voice assistant for your phone, in English, Hindi and Hinglish.") {
                        Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Image(painterResource(R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(180.dp))
                        }
                        Primary("Get started", next)
                    }
                    Step.Permissions -> PermissionsStep(next)
                    Step.Key -> Page("Your Gemini key", "${Branding.ASSISTANT_NAME} uses Google Gemini, with your own key, for anything the phone can't do by itself.") {
                        if (hasKey) {
                            Text("Your key is saved and checked.", color = Accent, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(16.dp))
                            Primary("Continue", next)
                        } else {
                            KeyForm(onSave = { key -> assistant.saveKey(key).also { if (it == null) next() } })
                        }
                    }
                    Step.LocalCloud -> Page("What runs where", "Simple commands never leave the phone. The rest goes to Gemini.") {
                        Section(null) {
                            InfoRow("On this phone", "Volume, music, flashlight, opening apps, time, battery, timers and alarms. No internet, no Gemini.") {
                                Pill("Local", Accent)
                            }
                            InfoRow("With Gemini", "Questions, calls and messages, songs by name, your screen. Needs internet and uses your key.") {
                                Pill("Gemini", Warning)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Primary("Continue", next)
                    }
                    Step.Done -> Page("You're set", "Say \"${WakeWordModel.PHRASE}\" or slide the button to talk. Settings has everything you chose here.") {
                        Spacer(Modifier.height(16.dp))
                        Primary("Start", onFinished)
                    }
                }
            }
        }
    }
}

@Composable
private fun Page(title: String, detail: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.displaySmall)
    Text(detail, style = MaterialTheme.typography.bodyLarge, color = Muted, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
    Column(content = content)
}

@Composable
private fun Primary(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(label) }
}

@Composable
private fun Progress(index: Int, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
        Step.entries.forEach {
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(if (it.ordinal <= index) Accent else MaterialTheme.colorScheme.outlineVariant, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
            )
        }
    }
}
