package com.example.jee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jee.data.JeeRepository
import com.example.jee.data.PlannerTask
import com.example.jee.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class JeeScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ROADMAP("Roadmap", Icons.Default.AltRoute),
    TODAY("Today Focus", Icons.Default.PlayCircleFilled),
    PLANNER("Planner", Icons.Default.CalendarMonth),
    SYLLABUS("Syllabus", Icons.Default.MenuBook),
    BACKLOG("Backlog", Icons.Default.WarningAmber),
    TESTS("Tests", Icons.Default.Assignment),
    ANALYTICS("Analytics", Icons.Default.Leaderboard),
    MISTAKES("Mistakes", Icons.Default.ReportProblem),
    AI_COACH("AI Coach", Icons.Default.SmartToy),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainJeeApp() {
    val context = LocalContext.current
    val repository = remember { JeeRepository(context) }
    val userProfile by repository.userProfile.collectAsState()

    var currentScreen by remember { mutableStateOf(JeeScreen.DASHBOARD) }
    var showOnboarding by remember { mutableStateOf(!userProfile.isOnboarded) }
    var showReplannerDialog by remember { mutableStateOf(false) }
    var activeExecutionTask by remember { mutableStateOf<PlannerTask?>(null) }
    var isExecutionModeFullScreen by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (showOnboarding) {
        OnboardingScreen(
            repository = repository,
            onFinish = { showOnboarding = false }
        )
        return
    }

    if (isExecutionModeFullScreen) {
        ExecutionModeScreen(
            repository = repository,
            initialTask = activeExecutionTask,
            onExitExecutionMode = {
                isExecutionModeFullScreen = false
                activeExecutionTask = null
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "JEE COMMAND CENTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Navigation Console",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = userProfile.studentName,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                JeeScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                                tint = if (isSelected) CyanNeon else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanNeon else TextPrimary
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            currentScreen = screen
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = DarkSurfaceElevated,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = DarkBg,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.title.uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "JEE COMMAND CENTER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanNeon,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentScreen = JeeScreen.AI_COACH }) {
                            Icon(Icons.Default.SmartToy, contentDescription = "AI Coach", tint = CyanNeon)
                        }
                        IconButton(onClick = { showReplannerDialog = true }) {
                            Icon(Icons.Default.SwapCalls, contentDescription = "Replan", tint = AmberGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBg,
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val bottomItems = listOf(
                        JeeScreen.DASHBOARD,
                        JeeScreen.PLANNER,
                        JeeScreen.SYLLABUS,
                        JeeScreen.TESTS,
                        JeeScreen.AI_COACH
                    )

                    bottomItems.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBg,
                                selectedTextColor = CyanNeon,
                                indicatorColor = CyanNeon,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    JeeScreen.DASHBOARD -> DashboardScreen(
                        repository = repository,
                        onStartExecutionMode = { task ->
                            activeExecutionTask = task
                            isExecutionModeFullScreen = true
                        },
                        onOpenReplannerDialog = { showReplannerDialog = true },
                        onNavigateToRoadmap = { currentScreen = JeeScreen.ROADMAP },
                        onNavigateToSyllabus = { currentScreen = JeeScreen.SYLLABUS },
                        onNavigateToBacklog = { currentScreen = JeeScreen.BACKLOG },
                        onNavigateToTests = { currentScreen = JeeScreen.TESTS },
                        onNavigateToAiCoach = { currentScreen = JeeScreen.AI_COACH }
                    )

                    JeeScreen.ROADMAP -> RoadmapScreen(repository = repository)

                    JeeScreen.TODAY -> ExecutionModeScreen(
                        repository = repository,
                        initialTask = null,
                        onExitExecutionMode = { currentScreen = JeeScreen.DASHBOARD }
                    )

                    JeeScreen.PLANNER -> PlannerScreen(
                        repository = repository,
                        onStartFocusSession = { task ->
                            activeExecutionTask = task
                            isExecutionModeFullScreen = true
                        },
                        onOpenReplannerDialog = { showReplannerDialog = true }
                    )

                    JeeScreen.SYLLABUS -> SyllabusScreen(repository = repository)

                    JeeScreen.BACKLOG -> BacklogScreen(
                        repository = repository,
                        onStartFocusSession = { task ->
                            activeExecutionTask = task
                            isExecutionModeFullScreen = true
                        }
                    )

                    JeeScreen.TESTS -> TestsScreen(repository = repository)

                    JeeScreen.ANALYTICS -> AnalyticsScreen(repository = repository)

                    JeeScreen.MISTAKES -> MistakesScreen(repository = repository)

                    JeeScreen.AI_COACH -> AiCoachScreen(repository = repository)

                    JeeScreen.SETTINGS -> SettingsScreen(
                        repository = repository,
                        onRerunOnboarding = { showOnboarding = true }
                    )
                }
            }
        }
    }

    if (showReplannerDialog) {
        AutoReplannerDialog(
            repository = repository,
            onDismiss = { showReplannerDialog = false }
        )
    }
}
