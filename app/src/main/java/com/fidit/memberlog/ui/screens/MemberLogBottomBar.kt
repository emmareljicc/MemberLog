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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val BarHeight = 64.dp
private val Overhang = 18.dp
private val ButtonSize = 56.dp
private val BarCornerRadius = 28.dp
private val NotchGap = 8.dp
private val NotchFillet = 18.dp

val NavBarSpace = 88.dp

data class BottomDest(val icon: ImageVector, val contentDescription: String, val badgeCount: Int = 0)

@Composable
fun MemberLogBottomBar(
    destinations: List<BottomDest>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null,
    addExpanded: Boolean = false
) {
    if (onAddClick == null) {
        PlainBottomBar(destinations, selectedIndex, onSelect, modifier)
        return
    }
    val density = LocalDensity.current
    val cornerPx = with(density) { BarCornerRadius.toPx() }
    val crPx = with(density) { (ButtonSize / 2f + NotchGap).toPx() }
    val frPx = with(density) { NotchFillet.toPx() }
    val fyPx = with(density) { (ButtonSize / 2f - Overhang).toPx() }

    val pillShape = remember(cornerPx, crPx, frPx, fyPx) {
        GenericShape { size, _ ->
            val path = this
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val xf = sqrt((crPx + frPx) * (crPx + frPx) - (frPx - fyPx) * (frPx - fyPx))
            val filletEndL = Math.toDegrees(atan2((fyPx - frPx).toDouble(), xf.toDouble()))
            val cradleStartL = Math.toDegrees(atan2((frPx - fyPx).toDouble(), (-xf).toDouble()))
            val cradleEndR = Math.toDegrees(atan2((frPx - fyPx).toDouble(), xf.toDouble()))
            var cradleSweep = cradleEndR - cradleStartL
            if (cradleSweep > 0.0) cradleSweep -= 360.0
            val filletStartR = Math.toDegrees(atan2((fyPx - frPx).toDouble(), (-xf).toDouble()))

            fun arc(cx0: Float, cy0: Float, r: Float, startDeg: Double, sweepDeg: Double) {
                val n = 40
                for (i in 0..n) {
                    val a = Math.toRadians(startDeg + sweepDeg * i / n)
                    path.lineTo(cx0 + r * cos(a).toFloat(), cy0 + r * sin(a).toFloat())
                }
            }

            moveTo(cornerPx, 0f)
            lineTo(cx - xf, 0f)
            arc(cx - xf, frPx, frPx, -90.0, filletEndL + 90.0)
            arc(cx, fyPx, crPx, cradleStartL, cradleSweep)
            arc(cx + xf, frPx, frPx, filletStartR, -90.0 - filletStartR)
            lineTo(w - cornerPx, 0f)
            quadraticTo(w, 0f, w, cornerPx)
            lineTo(w, h)
            lineTo(0f, h)
            lineTo(0f, cornerPx)
            quadraticTo(0f, 0f, cornerPx, 0f)
            close()
        }
    }

    val leftCount = (destinations.size + 1) / 2

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(BarHeight + Overhang)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BarHeight)
                .background(MaterialTheme.colorScheme.background)
        )
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
                .shadow(elevation = 12.dp, shape = CircleShape)
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
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(BarHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .shadow(elevation = 12.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = BarCornerRadius, topEnd = BarCornerRadius), clip = false)
                .background(color = MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = BarCornerRadius, topEnd = BarCornerRadius))
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
