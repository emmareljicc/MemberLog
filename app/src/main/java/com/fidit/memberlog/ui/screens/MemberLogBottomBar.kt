package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val BarHeight = 64.dp
private val Overhang = 32.dp
private val ButtonSize = 64.dp
private val CornerRadius = 30.dp
private val NotchRadius = 40.dp

/**
 * Floating bottom navigation: a rounded "pill" with a soft shadow and a concave
 * notch at the top-center, where the prominent circular Add button is docked.
 * Two icon-only destinations flank the button; the active one is tinted and
 * marked with a small dot. Uses only existing theme colors.
 */
@Composable
fun MemberLogBottomBar(
    selectedTab: Int,
    onMembersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val density = LocalDensity.current
    val notchPx = with(density) { NotchRadius.toPx() }
    val cornerPx = with(density) { CornerRadius.toPx() }

    // Rounded rectangle with a semicircular concave cutout at the top-center.
    val pillShape = remember(notchPx, cornerPx) {
        GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val r = notchPx
            val c = cornerPx

            moveTo(c, 0f)
            lineTo(cx - r, 0f)
            // dip down into the bar and back up, forming the notch
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(cx - r, -r, cx + r, r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(w - c, 0f)
            quadraticBezierTo(w, 0f, w, c)
            lineTo(w, h - c)
            quadraticBezierTo(w, h, w - c, h)
            lineTo(c, h)
            quadraticBezierTo(0f, h, 0f, h - c)
            lineTo(0f, c)
            quadraticBezierTo(0f, 0f, c, 0f)
            close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(BarHeight + Overhang)
    ) {
        // The floating pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BarHeight)
                .shadow(elevation = 12.dp, shape = pillShape, clip = false)
                .background(color = MaterialTheme.colorScheme.surface, shape = pillShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BarHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BottomNavItem(
                        icon = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Članovi",
                        selected = selectedTab == 0,
                        onClick = onMembersClick
                    )
                }
                Spacer(modifier = Modifier.width(ButtonSize + 16.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BottomNavItem(
                        icon = Icons.Default.Settings,
                        contentDescription = "Postavke",
                        selected = selectedTab == 1,
                        onClick = onSettingsClick
                    )
                }
            }
        }

        // The docked central Add button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(ButtonSize)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj člana",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
    }
}
