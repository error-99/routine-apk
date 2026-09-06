package com.studentapp.isu14.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.studentapp.isu14.R
import com.studentapp.isu14.ui.theme.AmberAccent
import com.studentapp.isu14.ui.theme.IndigoLight
import com.studentapp.isu14.ui.viewmodel.AppTab

@Composable
fun BottomNavBar(
    currentTab: AppTab,
    unreadNotificationsCount: Int,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = MaterialTheme.colorScheme.surfaceTint.let { 4.androidx.compose.ui.unit.dp }
    ) {
        // Routine
        NavigationBarItem(
            modifier = Modifier.testTag("tab_routine"),
            selected = currentTab == AppTab.ROUTINE,
            onClick = { onTabSelected(AppTab.ROUTINE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.ROUTINE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = stringResource(R.string.nav_routine)
                )
            },
            label = { Text(stringResource(R.string.nav_routine)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoLight,
                selectedTextColor = IndigoLight,
                indicatorColor = IndigoLight.copy(alpha = 0.15f)
            )
        )

        // Courses
        NavigationBarItem(
            modifier = Modifier.testTag("tab_courses"),
            selected = currentTab == AppTab.COURSES,
            onClick = { onTabSelected(AppTab.COURSES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.COURSES) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                    contentDescription = stringResource(R.string.nav_courses)
                )
            },
            label = { Text(stringResource(R.string.nav_courses)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoLight,
                selectedTextColor = IndigoLight,
                indicatorColor = IndigoLight.copy(alpha = 0.15f)
            )
        )

        // Notifications
        NavigationBarItem(
            modifier = Modifier.testTag("tab_notifications"),
            selected = currentTab == AppTab.NOTIFICATIONS,
            onClick = { onTabSelected(AppTab.NOTIFICATIONS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(
                                containerColor = AmberAccent,
                                contentColor = MaterialTheme.colorScheme.surface
                            ) {
                                Text("$unreadNotificationsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == AppTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.nav_notifications)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_notifications)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoLight,
                selectedTextColor = IndigoLight,
                indicatorColor = IndigoLight.copy(alpha = 0.15f)
            )
        )

        // Profile
        NavigationBarItem(
            modifier = Modifier.testTag("tab_profile"),
            selected = currentTab == AppTab.PROFILE,
            onClick = { onTabSelected(AppTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.nav_profile)
                )
            },
            label = { Text(stringResource(R.string.nav_profile)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoLight,
                selectedTextColor = IndigoLight,
                indicatorColor = IndigoLight.copy(alpha = 0.15f)
            )
        )
    }
}
