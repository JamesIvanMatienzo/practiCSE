package com.jigen.practicse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun MainApp(context: ComponentActivity) {
    val navController = rememberNavController()

    Scaffold { paddingValues ->
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
