package com.fidit.memberlog.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val BarHeight = 64.dp
private val Overhang = 10.dp
private val ButtonSize = 56.dp
private val CornerRadius = 28.dp
private val NotchRadius = 34.dp

data class BottomDest(val icon: ImageVector, val contentDescription: String, val badgeCount: Int = 0)

@Composable
fun MemberLogBottomBar(
    destinations: List<BottomDest>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddClick: (() -> Unit)? = null,
    addExpanded: Boolean = false
) {
    if (onAddClick == null) {
        PlainBottomBar(destinations, selectedIndex, onSelect)
        return
    }
    val density = LocalDensity.current
    val notchPx = with(density) { NotchRadius.toPx() }
    val cornerPx = with(density) { CornerRadius.toPx() }

    val pillShape = remember(notchPx, cornerPx) {
        GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val r = notchPx
            val c = cornerPx
            moveTo(c, 0f)
            lineTo(cx - r, 0f)
            arcTo(
                rect = Rect(cx - r, -r, cx + r, r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(w - c, 0f)
            quadraticTo(w, 0f, w, c)
            lineTo(w, h - c)
            quadraticTo(w, h, w - c, h)
            lineTo(c, h)
            quadraticTo(0f, h, 0f, h - c)
            lineTo(0f, c)
            quadraticTo(0f, 0f, c, 0f)
            close()
        }
    }

    val leftCount = (destinations.size + 1) / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(BarHeight + Overhang)
    ) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.take(leftCount).forEachIndexed { i, dest ->
                        BottomNavItem(dest.icon, dest.contentDescription, selectedIndex == i, dest.badgeCount) { onSelect(i) }
                    }
                }
                Spacer(modifier = Modifier.width(ButtonSize + 16.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.drop(leftCount).forEachIndexed { i, dest ->
                        val index = leftCount + i
                        BottomNavItem(dest.icon, dest.contentDescription, selectedIndex == index, dest.badgeCount) { onSelect(index) }
                    }
                }
            }
        }

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
            val rotation by animateFloatAsState(if (addExpanded) 45f else 0f, label = "fabRotate")
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }
}

@Composable
private fun PlainBottomBar(
    destinations: List<BottomDest>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(BarHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .shadow(elevation = 12.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius), clip = false)
                .background(color = MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BarHeight),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { i, dest ->
                    BottomNavItem(dest.icon, dest.contentDescription, selectedIndex == i, dest.badgeCount) { onSelect(i) }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge { Text("$badgeCount") }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
    }
}
