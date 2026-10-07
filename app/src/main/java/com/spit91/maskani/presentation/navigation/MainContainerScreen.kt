package com.spit91.maskani.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spit91.maskani.presentation.auth.home.HomeScreen
import com.spit91.maskani.presentation.auth.home.SavedScreen
import com.spit91.maskani.presentation.auth.home.ProfileScreen
import com.spit91.maskani.presentation.auth.home.detail.PropertyDetailScreen
import com.spit91.maskani.presentation.auth.home.requests.MyRequestsScreen

// FIX: Renamed from NavigationItem to ContainerTab to resolve the "Redeclaration" conflict!
private sealed class ContainerTab(
    val route: Screen,
    val title: String,
    val icon: ImageVector
) {
    object HomeTab : ContainerTab(Screen.Home, "Explore", Icons.Default.Home)
    object SavedTab : ContainerTab(Screen.Saved, "Saved", Icons.Default.Favorite)
    object ViewingsTab : ContainerTab(Screen.MyRequests, "Viewings", Icons.Default.CalendarMonth)
    object ProfileTab : ContainerTab(Screen.Profile, "Profile", Icons.Default.Person)
}

@Composable
fun MainContainerScreen(
    //make a callback to the root navigation controller
    onRootLogoutTriggered: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val bottomTabNavController = rememberNavController() // remember the nav controller state
    val navBackStackEntry by bottomTabNavController.currentBackStackEntryAsState() // get the current backstack entry
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        ContainerTab.HomeTab,
        ContainerTab.SavedTab,
        ContainerTab.ViewingsTab,
        ContainerTab.ProfileTab
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    // Type-safe matching check using modernhasRoute feature
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.hasRoute(item.route::class)
                    } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            bottomTabNavController.navigate(item.route) {
                                popUpTo(bottomTabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomTabNavController,
            startDestination = Screen.Home,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onPropertyClick = { id -> bottomTabNavController.navigate(Screen.PropertyDetail(id)) }
                )
            }
            composable<Screen.PropertyDetail> {
                PropertyDetailScreen(
                    onNavigateBack = { bottomTabNavController.popBackStack() },
                    onViewMyRequests = {
                        bottomTabNavController.navigate(Screen.MyRequests) {
                            popUpTo(bottomTabNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable<Screen.Saved> { SavedScreen() }
            composable<Screen.MyRequests> {
                MyRequestsScreen(
                    onPropertyClick = { id -> bottomTabNavController.navigate(Screen.PropertyDetail(id)) }
                )
            }
            composable<Screen.Profile> {
                ProfileScreen(
                onLogoutSuccess = {
                    // Trigger the logout sequence
                    onRootLogoutTriggered()
                },
                    onEditProfileClick ={
                       onEditProfileClick()
                    }

            ) }
        }
    }
}
