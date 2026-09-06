package com.studentapp.isu14

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studentapp.isu14.ui.components.BottomNavBar
import com.studentapp.isu14.ui.components.OfflineConnectionDialog
import com.studentapp.isu14.ui.components.OfflineStatusBar
import com.studentapp.isu14.ui.screens.AuthScreen
import com.studentapp.isu14.ui.screens.CoursesScreen
import com.studentapp.isu14.ui.screens.NotificationsScreen
import com.studentapp.isu14.ui.screens.ProfileScreen
import com.studentapp.isu14.ui.screens.RoutineScreen
import com.studentapp.isu14.ui.theme.ISURoutineTheme
import com.studentapp.isu14.ui.viewmodel.AppTab
import com.studentapp.isu14.ui.viewmodel.CoursesViewModel
import com.studentapp.isu14.ui.viewmodel.CoursesViewModelFactory
import com.studentapp.isu14.ui.viewmodel.MainViewModel
import com.studentapp.isu14.ui.viewmodel.MainViewModelFactory
import com.studentapp.isu14.ui.viewmodel.NotificationsViewModel
import com.studentapp.isu14.ui.viewmodel.NotificationsViewModelFactory
import com.studentapp.isu14.ui.viewmodel.RoutineViewModel
import com.studentapp.isu14.ui.viewmodel.RoutineViewModelFactory

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        val app = application as IsuRoutineApp
        MainViewModelFactory(app.repository, app.networkMonitor, app.syncManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ISURoutineTheme {
                MainApp(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun MainApp(mainViewModel: MainViewModel) {
    val currentStudent by mainViewModel.currentStudent.collectAsState()
    val currentTab by mainViewModel.currentTab.collectAsState()
    val isOnline by mainViewModel.isOnline.collectAsState()
    val showOfflineDialog by mainViewModel.showOfflineDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Resume hook for auto-syncing when opening app, sleeping otherwise
    val lifecycleOwner = LocalLifecycleOwner.current
    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.onAppResume()
                // Also trigger widget refresh
                try {
                    com.studentapp.isu14.widget.RoutineWidgetProvider.updateAllWidgets(appContext)
                } catch (_: Exception) {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.toastFlow.collect { toast ->
            snackbarHostState.showSnackbar(message = toast.message)
        }
    }

    // 2-second offline dialog prompt
    if (showOfflineDialog) {
        OfflineConnectionDialog(
            onDismiss = { mainViewModel.dismissOfflineDialog() },
            onRetry = { mainViewModel.retryConnection() }
        )
    }

    val repository = (androidx.compose.ui.platform.LocalContext.current.applicationContext as IsuRoutineApp).repository

    if (currentStudent == null) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                OfflineStatusBar(
                    isOnline = isOnline,
                    onManualSync = { mainViewModel.retryConnection() }
                )
                AuthScreen(
                    mainViewModel = mainViewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        val student = currentStudent!!

        // Routine ViewModel
        val routineViewModel: RoutineViewModel = viewModel(
            key = "routine_${student.studentId}_${student.department}_${student.semesterId}",
            factory = RoutineViewModelFactory(repository, student.studentId, student.department)
        )

        // Courses ViewModel
        val coursesViewModel: CoursesViewModel = viewModel(
            key = "courses_${student.studentId}_${student.department}_${student.semesterId}",
            factory = CoursesViewModelFactory(repository, student.studentId, student.department, student.semesterId)
        )

        // Notifications ViewModel
        val notificationsViewModel: NotificationsViewModel = viewModel(
            key = "notifications_${student.department}",
            factory = NotificationsViewModelFactory(repository, student.department)
        )

        val unreadNoticesCount by notificationsViewModel.unreadCount.collectAsState()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BottomNavBar(
                    currentTab = currentTab,
                    unreadNotificationsCount = unreadNoticesCount,
                    onTabSelected = { mainViewModel.selectTab(it) }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                OfflineStatusBar(
                    isOnline = isOnline,
                    onManualSync = { mainViewModel.retryConnection() }
                )
                when (currentTab) {
                    AppTab.ROUTINE -> RoutineScreen(
                        viewModel = routineViewModel,
                        student = student,
                        modifier = Modifier.weight(1f)
                    )
                    AppTab.COURSES -> CoursesScreen(
                        viewModel = coursesViewModel,
                        student = student,
                        onShowToast = { mainViewModel.showToast(it) },
                        modifier = Modifier.weight(1f)
                    )
                    AppTab.NOTIFICATIONS -> NotificationsScreen(
                        viewModel = notificationsViewModel,
                        modifier = Modifier.weight(1f)
                    )
                    AppTab.PROFILE -> ProfileScreen(
                        mainViewModel = mainViewModel,
                        student = student,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
