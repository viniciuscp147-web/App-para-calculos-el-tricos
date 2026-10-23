package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthAndTutorialScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.PowerOhmScreen
import com.example.ui.screens.SupplierTablesScreen
import com.example.ui.screens.VoltageDropScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.CableViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: CableViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val budgetItems by viewModel.budgetItems.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Return to main screen on back press
    if (currentTab == AppTab.TABLES) {
        BackHandler { viewModel.setTab(AppTab.CABLE_PRICES) }
    } else if (currentTab != AppTab.VOLTAGE_DROP) {
        BackHandler { viewModel.setTab(AppTab.VOLTAGE_DROP) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // DESKTOP / PC FULLSCREEN LAYOUT (NavigationRail on Left + Full Width Content)
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Eletric",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    // Item 1: Voltage Drop
                    NavigationRailItem(
                        selected = currentTab == AppTab.VOLTAGE_DROP,
                        onClick = { viewModel.setTab(AppTab.VOLTAGE_DROP) },
                        icon = { Icon(Icons.Default.TrendingDown, contentDescription = "Queda Tensão") },
                        label = { Text("Queda Tensão", fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Item 2: Cálculos de Proporção
                    NavigationRailItem(
                        selected = currentTab == AppTab.POWER_OHM,
                        onClick = { viewModel.setTab(AppTab.POWER_OHM) },
                        icon = { Icon(Icons.Default.ElectricBolt, contentDescription = "Cálculos de Proporção") },
                        label = { Text("Proporção", fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Item 3: Cables & Prices
                    NavigationRailItem(
                        selected = currentTab == AppTab.CABLE_PRICES || currentTab == AppTab.TABLES,
                        onClick = { viewModel.setTab(AppTab.CABLE_PRICES) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Cabos") },
                        label = { Text("Cabos", fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Item 4: Budget
                    NavigationRailItem(
                        selected = currentTab == AppTab.BUDGET,
                        onClick = { viewModel.setTab(AppTab.BUDGET) },
                        icon = {
                            if (budgetItems.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text(budgetItems.size.toString()) } }) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Orçamento")
                                }
                            } else {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Orçamento")
                            }
                        },
                        label = { Text("Orçamento", fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Item 5: Account / Settings
                    NavigationRailItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.setTab(AppTab.SETTINGS) },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Conta") },
                        label = { Text("Conta", fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                // Desktop Main Content Area
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Eletric Calculator - Desktop",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppScreenContent(currentTab = currentTab, viewModel = viewModel)
                    }
                }
            }
        } else {
            // MOBILE / COMPACT SCREEN LAYOUT (NavigationBar on Bottom)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "Eletric Calculator",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.VOLTAGE_DROP,
                            onClick = { viewModel.setTab(AppTab.VOLTAGE_DROP) },
                            icon = { Icon(Icons.Default.TrendingDown, contentDescription = "Queda Tensão") },
                            label = { Text("Queda Tensão", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.POWER_OHM,
                            onClick = { viewModel.setTab(AppTab.POWER_OHM) },
                            icon = { Icon(Icons.Default.ElectricBolt, contentDescription = "Cálculos de Proporção") },
                            label = { Text("Proporção", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.CABLE_PRICES || currentTab == AppTab.TABLES,
                            onClick = { viewModel.setTab(AppTab.CABLE_PRICES) },
                            icon = { Icon(Icons.Default.Calculate, contentDescription = "Cabos") },
                            label = { Text("Cabos", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.BUDGET,
                            onClick = { viewModel.setTab(AppTab.BUDGET) },
                            icon = {
                                if (budgetItems.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text(budgetItems.size.toString()) } }) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = "Orçamento")
                                    }
                                } else {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Orçamento")
                                }
                            },
                            label = { Text("Orçamento", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.SETTINGS,
                            onClick = { viewModel.setTab(AppTab.SETTINGS) },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Conta") },
                            label = { Text("Conta", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AppScreenContent(currentTab = currentTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppScreenContent(
    currentTab: AppTab,
    viewModel: CableViewModel
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "tab_animation"
    ) { tab ->
        when (tab) {
            AppTab.VOLTAGE_DROP -> VoltageDropScreen(viewModel = viewModel)
            AppTab.POWER_OHM -> PowerOhmScreen(viewModel = viewModel)
            AppTab.CABLE_PRICES -> CalculatorScreen(viewModel = viewModel)
            AppTab.TABLES -> SupplierTablesScreen(viewModel = viewModel)
            AppTab.BUDGET -> BudgetScreen(viewModel = viewModel)
            AppTab.SETTINGS -> AuthAndTutorialScreen(viewModel = viewModel)
        }
    }
}
