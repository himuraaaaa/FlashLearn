package com.pam.flashlearn.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pam.flashlearn.R
import com.pam.flashlearn.ui.theme.Primary
import com.pam.flashlearn.ui.theme.PrimaryLight
import com.pam.flashlearn.ui.theme.White

data class NavItem(
    val title: String,
    val selectedIconResId: Int,
    val unselectedIconResId: Int,
    val route: String
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavItemClick: (String) -> Unit
) {
    val navItems = listOf(
        NavItem(
            "Home",
            R.drawable.ic_home_selected,
            R.drawable.ic_home_unselected,
            "home"
        ),
        NavItem(
            "Album",
            R.drawable.ic_album_selected,
            R.drawable.ic_album_unselected,
            "album"
        ),
        NavItem(
            "Add",
            R.drawable.ic_add_selected,
            R.drawable.ic_add_unselected,
            "create_set"
        ),
        NavItem(
            "Profile",
            R.drawable.ic_profile_selected,
            R.drawable.ic_profile_unselected,
            "profile"
        )
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        containerColor = White
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selected) item.selectedIconResId else item.unselectedIconResId
                        ),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified // No tint to preserve custom icon colors
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) Primary else Color.Gray
                    )
                },
                selected = selected,
                onClick = { onNavItemClick(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryLight.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}