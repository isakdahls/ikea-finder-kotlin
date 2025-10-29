package se.isakdahls.ikeafinder.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import se.isakdahls.ikeafinder.map.MapScreen
import se.isakdahls.ikeafinder.list.StoreListScreen
import se.isakdahls.ikeafinder.detail.StoreDetailScreen

/**
 * navigering
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(

        navController = navController,
        startDestination = "map",
        modifier = modifier
    ) {
        composable("map") {

            MapScreen(
                onNavigateToList = { 
                    navController.navigate("list") {
                        launchSingleTop = true
                    }
                },
                onNavigateToDetail = { storeId ->
                    navController.navigate("detail/$storeId") {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable("list") {
            StoreListScreen(
                onNavigateToDetail = { storeId ->
                    navController.navigate("detail/$storeId") {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            "detail/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getInt("storeId") ?: 0
            StoreDetailScreen(
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}