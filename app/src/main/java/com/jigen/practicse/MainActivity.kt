package com.jigen.practicse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jigen.practicse.ui.navigation.NavGraph
import com.jigen.practicse.ui.navigation.Screen
import com.jigen.practicse.ui.theme.PractiCSETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PractiCSETheme {
                MainApp(context = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(context: ComponentActivity) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showMenu by remember { mutableStateOf(false) }
    val showTopBar = currentRoute == Screen.Dashboard.route

    Scaffold(
        topBar = {
            if (showTopBar) {
                DashboardTopBar(
                    showMenu = showMenu,
                    onMenuChange = { showMenu = it },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavGraph(
                navController = navController,
                context = context,
                startDestination = Screen.Login.route
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    showMenu: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onNavigate: (String) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "practiCSE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        },
        actions = {
            IconButton(onClick = { onMenuChange(true) }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onMenuChange(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        onMenuChange(false)
                        onNavigate(Screen.Profile.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        onMenuChange(false)
                        onNavigate(Screen.Settings.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("About") },
                    onClick = {
                        onMenuChange(false)
                        onNavigate(Screen.About.route)
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            actionIconContentColor = Color(0xFF202124)
        )
    )
}
