package com.kmj.jarvis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmj.jarvis.Branding
import com.kmj.jarvis.R
import com.kmj.jarvis.assistant.AssistantViewModel
import com.kmj.jarvis.assistant.UiState
import com.kmj.jarvis.local.ActivityLog
import com.kmj.jarvis.local.LocalPhase
import com.kmj.jarvis.task.Task
import kotlinx.coroutines.launch

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Automations("Automations", Icons.Filled.Star),
    Activity("Activity", Icons.Filled.List),
    Settings("Settings", Icons.Filled.Settings),
}

/** Root shell: splash → onboarding → drawer shell with a left sidebar instead of tabs. */
@Composable
fun KMJApp(assistant: AssistantViewModel) {
    val state by assistant.state.collectAsStateWithLifecycle()
    val localPhase by assistant.localPhase.collectAsStateWithLifecycle()
    val activity by assistant.activity.collectAsStateWithLifecycle()
    val wakeWord by assistant.wakeWord.collectAsStateWithLifecycle()
    val level by assistant.level.collectAsStateWithLifecycle()
    val task by assistant.task.collectAsStateWithLifecycle()
    var splashed by rememberSaveable { mutableStateOf(false) }
    var onboarded by rememberSaveable { mutableStateOf(assistant.onboarded) }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    KMJTheme {
        Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
            when {
                !splashed -> Splash { splashed = true }
                !onboarded || !state.hasKey -> Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Onboarding(assistant, state.hasKey) {
                        assistant.onboarded = true
                        onboarded = true
                    }
                }
                else -> DrawerShell(
                    assistant, state, localPhase, level, task, activity, wakeWord, tab,
                ) { tab = it }
            }
        }
    }
}

@Composable
private fun DrawerShell(
    assistant: AssistantViewModel,
    state: UiState,
    localPhase: LocalPhase,
    level: Float,
    task: Task?,
    activity: List<ActivityLog.Entry>,
    wakeWord: Boolean,
    tab: Int,
    setTab: (Int) -> Unit,
) {
    val drawer = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawer.open() } }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(52.dp))
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(Branding.ASSISTANT_NAME, style = MaterialTheme.typography.titleMedium)
                        Text("Your phone. Your AI. One voice.", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
                Spacer(Modifier.size(8.dp))
                Tab.entries.forEach { entry ->
                    NavigationDrawerItem(
                        label = { Text(entry.label) },
                        icon = { Icon(entry.icon, contentDescription = null) },
                        selected = tab == entry.ordinal,
                        onClick = {
                            setTab(entry.ordinal)
                            scope.launch { drawer.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                val v = versionLabel()
                Text(
                    "v$v · Gemini powered",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                    modifier = Modifier.padding(16.dp),
                )
            }
        },
    ) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (Tab.entries[tab]) {
                    Tab.Home -> HomeScreen(
                        state = state,
                        localPhase = localPhase,
                        level = level,
                        task = task,
                        activity = activity,
                        wakeOn = assistant.wakeWordAvailable && wakeWord,
                        onStart = assistant::start,
                        onStop = assistant::stop,
                        onInterrupt = assistant::interrupt,
                        onMicDenied = assistant::micUnavailable,
                        onPermissionAnswered = assistant::permissionAnswered,
                        onQuick = assistant::quick,
                        onSeeActivity = { setTab(Tab.Activity.ordinal) },
                        onOpenDrawer = openDrawer,
                        favourites = remember(activity.size) { assistant.favourites() },
                    )
                    Tab.Automations -> TabPage("Automations", openDrawer) { AutomationsScreen() }
                    Tab.Activity -> TabPage("Activity", openDrawer) { ActivityScreen(activity, assistant::clearActivity) }
                    Tab.Settings -> TabPage("Settings", openDrawer) { SettingsScreen(assistant) }
                }
            }
        }
    }
}

/** A non-Home screen: hamburger + title header, content below. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabPage(title: String, onOpenDrawer: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun versionLabel(): String {
    val context = LocalContext.current
    return remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
    }
}
