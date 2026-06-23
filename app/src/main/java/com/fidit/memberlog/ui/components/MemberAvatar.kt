package com.fidit.memberlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun MemberAvatar(
    name: String,
    photoPath: String?,
    color: Color,
    size: Dp,
    fontSize: TextUnit = 16.sp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        } else {
            Text(
                text = initials(name),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}

private fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.size > 1 -> "${parts[0][0]}${parts[1][0]}"
        parts.size == 1 -> parts[0].take(1)
        else -> "?"
    }
}
