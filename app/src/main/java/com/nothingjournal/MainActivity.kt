package com.nothingjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.nothingjournal.ui.animation.NothingOSEnter
import com.nothingjournal.ui.animation.NothingOSExit
import com.nothingjournal.ui.animation.NothingOSPopEnter
import com.nothingjournal.ui.animation.NothingOSPopExit
import com.nothingjournal.ui.screen.assistant.AssistantScreen
import com.nothingjournal.ui.screen.journal.JournalScreen
import com.nothingjournal.ui.screen.home.HomeScreen
import com.nothingjournal.ui.screen.insights.InsightsScreen
import com.nothingjournal.ui.screen.settings.SettingsScreen
import com.nothingjournal.ui.screen.editor.NoteEditorScreen
import com.nothingjournal.ui.screen.onboarding.OnboardingScreen
import com.nothingjournal.ui.theme.NothingJournalTheme
import dagger.hilt.android.AndroidEntryPoint

object Routes {
    const val NOTES = "notes"
    const val JOURNAL = "journal"
    const val ASSISTANT = "assistant"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    const val EDITOR = "editor?noteId={noteId}&templateId={templateId}"
    const val ONBOARDING = "onboarding"

    fun editor(noteId: Long? = null, templateId: String? = null): String {
        val params = mutableListOf<String>()
        if (noteId != null) params += "noteId=$noteId"
        if (templateId != null) params += "templateId=$templateId"
        return if (params.isEmpty()) "editor" else "editor?" + params.joinToString("&")
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NothingJournalTheme {
                JournalApp(viewModel)
            }
        }
    }
}

@Composable
private fun JournalApp(viewModel: AssistantViewModel) {
    val navController = rememberNavController()
    val startRoute: String? by viewModel.startRoute.collectAsState()

    if (startRoute == null) return // settings still loading

    NavHost(
        navController = navController,
        startDestination = startRoute ?: Routes.NOTES,
        enterTransition = NothingOSEnter,
        exitTransition = NothingOSExit,
        popEnterTransition = NothingOSPopEnter,
        popExitTransition = NothingOSPopExit,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = viewModel,
                onDone = {
                    viewModel.completeOnboarding()
                    navController.navigate(Routes.NOTES) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.NOTES) {
            HomeScreen(
                viewModel = viewModel,
                onOpenEditor = { navController.navigate(Routes.editor(it)) },
                onOpenEditorWithTemplate = { templateId ->
                    navController.navigate(Routes.editor(templateId = templateId))
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigate = { route ->
                    if (route != Routes.NOTES) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.JOURNAL) {
            JournalScreen(
                viewModel = viewModel,
                onNavigate = { route ->
                    if (route != Routes.JOURNAL) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.ASSISTANT) {
            AssistantScreen(
                viewModel = viewModel,
                onNavigate = { route ->
                    if (route != Routes.ASSISTANT) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.INSIGHTS) {
            InsightsScreen(
                onNavigate = { route ->
                    if (route != Routes.INSIGHTS) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.EDITOR,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("templateId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            val noteId = entry.arguments?.getLong("noteId") ?: -1L
            val templateId = entry.arguments?.getString("templateId").orEmpty()
            NoteEditorScreen(
                noteId = if (noteId == -1L) null else noteId,
                templateId = templateId.ifBlank { null },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
