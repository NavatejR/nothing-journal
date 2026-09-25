package com.nothingjournal.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nothingjournal.ui.components.DotMatrixText
import com.nothingjournal.ui.components.RedDot
import com.nothingjournal.ui.theme.NothingRed

enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    NOTES("notes", "Home", Icons.Outlined.Apps),
    JOURNAL("journal", "Journal", Icons.AutoMirrored.Outlined.MenuBook),
    ASSISTANT("assistant", "Assistant", Icons.AutoMirrored.Outlined.Chat),
    INSIGHTS("insights", "Insights", Icons.Outlined.AutoAwesome),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings),
}

@Composable
fun NothingBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Weighted slots so all five tabs fit on the narrowest phones — a plain
    // SpaceEvenly row clips the last tabs off-screen at 411dp.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomTab.entries.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavItem(
                label = tab.label,
                icon = tab.icon,
                isSelected = isSelected,
                onClick = { onNavigate(tab.route) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = NothingRed
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Active tab gets a red dot before the label (NothingOS signature)
            if (isSelected) {
                RedDot(modifier = Modifier.padding(end = 3.dp), size = 4.dp)
            }
            DotMatrixText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
