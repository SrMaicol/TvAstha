package com.asthatv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.asthatv.app.model.MediaItemUi

@Composable
fun DetailsScreen(
    media: MediaItemUi,
    onBack: () -> Unit,
    onPlayDemo: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = media.backdrop,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = .96f), Color.Black.copy(alpha = .72f), Color.Black.copy(alpha = .12f))
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .76f)))
            )
        )

        Column(
            Modifier.fillMaxHeight().width(650.dp).padding(start = 56.dp, top = 54.dp, bottom = 54.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(media.title, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Text(
                "${media.year}   •   ${media.rating} ★   •   ${media.genre}",
                color = Color.White.copy(alpha = .64f), fontSize = 15.sp
            )
            Spacer(Modifier.height(18.dp))
            Text(media.description, color = Color.White.copy(alpha = .82f), fontSize = 17.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TvActionButton("Reproducir demo", Icons.Outlined.PlayArrow, onPlayDemo)
                TvActionButton("Volver", Icons.Outlined.ArrowBack, onBack)
            }
        }
    }
}

@Composable
private fun TvActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused) Color.White else Color.White.copy(alpha = .14f))
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (focused) Color.Black else Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = if (focused) Color.Black else Color.White, fontWeight = FontWeight.SemiBold)
    }
}
