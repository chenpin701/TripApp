package com.tripapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tripapp.data.repository.TripRepository
import com.tripapp.ui.screens.home.HomeScreen
import com.tripapp.ui.screens.home.HomeViewModel
import com.tripapp.ui.screens.trip.TripDetailScreen
import com.tripapp.ui.screens.trip.TripDetailViewModel

@Composable
fun TripAppNavGraph(repository: TripRepository, currentUid: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val vm = viewModel { HomeViewModel(repository) }
            HomeScreen(
                viewModel = vm,
                currentUid = currentUid,
                onOpenTrip = { tripId -> navController.navigate("trip/$tripId") }
            )
        }
        composable(
            route = "trip/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            val vm = viewModel { TripDetailViewModel(repository, tripId) }
            TripDetailScreen(
                viewModel = vm,
                tripName = tripId, // 實際專案可從 repository 查一次行程名稱帶入
                onBack = { navController.popBackStack() }
            )
        }
    }
}
