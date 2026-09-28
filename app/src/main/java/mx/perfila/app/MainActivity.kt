package mx.perfila.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.perfila.app.domain.UserRole
import mx.perfila.app.ui.screens.CandidateOnboardingScreen
import mx.perfila.app.ui.screens.ChatScreen
import mx.perfila.app.ui.screens.MainScreen
import mx.perfila.app.ui.screens.PostJobScreen
import mx.perfila.app.ui.screens.RoleSelectScreen
import mx.perfila.app.ui.screens.WelcomeScreen
import mx.perfila.app.ui.theme.PerfilaColors
import mx.perfila.app.ui.theme.PerfilaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PerfilaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfilaTheme {
                Surface(Modifier.fillMaxSize(), color = PerfilaColors.Bone) {
                    PerfilaNavHost(viewModel)
                }
            }
        }
    }
}

private object Routes {
    const val WELCOME = "welcome"
    const val ROLE = "role"
    const val ONBOARDING = "onboarding"
    const val POST_JOB = "post-job"
    const val MAIN = "main"
    const val CHAT = "chat/{matchId}"
    fun chat(matchId: String) = "chat/${android.net.Uri.encode(matchId)}"
}

@Composable
private fun PerfilaNavHost(viewModel: PerfilaViewModel) {
    val nav = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    fun goMain() {
        nav.navigate(Routes.MAIN) {
            popUpTo(Routes.WELCOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = nav, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onStart = { nav.navigate(Routes.ROLE) })
        }
        composable(Routes.ROLE) {
            RoleSelectScreen(onContinue = { role ->
                viewModel.setRole(role)
                nav.navigate(if (role == UserRole.CANDIDATE) Routes.ONBOARDING else Routes.POST_JOB)
            })
        }
        composable(Routes.ONBOARDING) {
            CandidateOnboardingScreen(initial = state.me, onDone = { profile ->
                viewModel.updateProfile(profile)
                if (!nav.popBackStack(Routes.MAIN, inclusive = false)) goMain()
            })
        }
        composable(Routes.POST_JOB) {
            PostJobScreen(initial = state.myJob, onPublish = { job ->
                viewModel.publishJob(job)
                if (!nav.popBackStack(Routes.MAIN, inclusive = false)) goMain()
            })
        }
        composable(Routes.MAIN) {
            MainScreen(
                state = state,
                onSwipeJob = viewModel::swipeJob,
                onSwipeCandidate = viewModel::swipeCandidate,
                onOpenChat = { id -> nav.navigate(Routes.chat(id)) },
                onDismissMatch = viewModel::dismissPendingMatch,
                onEditProfile = { nav.navigate(Routes.ONBOARDING) },
                onEditJob = { nav.navigate(Routes.POST_JOB) },
                onSwitchRole = viewModel::setRole,
                onResetDecks = viewModel::resetDecks,
            )
        }
        composable(
            Routes.CHAT,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType }),
        ) { entry ->
            val matchId = entry.arguments?.getString("matchId").orEmpty()
            ChatScreen(
                match = state.matches.firstOrNull { it.id == matchId },
                role = state.role,
                onBack = { nav.popBackStack() },
                onSend = { text -> viewModel.sendMessage(matchId, text) },
            )
        }
    }
}
