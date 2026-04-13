package com.clipboardreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clipboardreminder.ui.screen.FieldListScreen
import com.clipboardreminder.ui.screen.ReminderListScreen
import com.clipboardreminder.ui.screen.SearchScreen
import com.clipboardreminder.ui.screen.SettingsScreen

sealed class Screen(val route: String) {
    object FieldList : Screen("field_list")
    object ReminderList : Screen("reminder_list/{fieldId}/{fieldName}/{isMostUsed}") {
        fun createRoute(fieldId: Long, fieldName: String, isMostUsed: Boolean) =
            "reminder_list/$fieldId/${fieldName.encodeUrl()}/$isMostUsed"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.FieldList.route) {
        composable(Screen.FieldList.route) {
            FieldListScreen(
                onNavigateToReminders = { fieldId, fieldName, isMostUsed ->
                    navController.navigate(Screen.ReminderList.createRoute(fieldId, fieldName, isMostUsed))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(
            route = Screen.ReminderList.route,
            arguments = listOf(
                navArgument("fieldId") { type = NavType.LongType },
                navArgument("fieldName") { type = NavType.StringType },
                navArgument("isMostUsed") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val fieldId = backStackEntry.arguments?.getLong("fieldId") ?: -1L
            val fieldName = backStackEntry.arguments?.getString("fieldName") ?: ""
            val isMostUsed = backStackEntry.arguments?.getBoolean("isMostUsed") ?: false
            ReminderListScreen(
                fieldId = fieldId,
                fieldName = fieldName.decodeUrl(),
                isMostUsed = isMostUsed,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.decodeUrl() = java.net.URLDecoder.decode(this, "UTF-8")
