package com.asthatv.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import androidx.compose.ui.platform.LocalContext
import com.asthatv.app.model.MediaItemUi

private val PosterShape = RoundedCornerShape(10.dp)

@Composable
fun PremiumPoster(media: MediaItemUi, onFocused: (MediaItemUi) -> Unit, onClick: (MediaItemUi) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.055f else 1f, tween(190, easing = FastOutSlowInEasing), label = "posterScale")
    val shadowDp by animateFloatAsState(if (focused) 22f else 2f, tween(190), label = "posterShadow")
    val overlay by animateColorAsState(if (focused) Color.Transparent else Color.Black.copy(alpha = .08f), tween(190), label = "posterOverlay")
    val density = LocalDensity.current
    val context = LocalContext.current
    val request = remember(media.poster, density.density) {
        ImageRequest.Builder(context)
            .data(media.poster)
            // Decode near display size instead of retaining oversized poster bitmaps.
            .size(Size((164 * density.density).toInt(), (246 * density.density).toInt()))
            .crossfade(false) // focus/scroll stays cheaper on low-end TV GPUs
            .build()
    }

    Box(
        modifier = Modifier
            .width(164.dp).height(246.dp)
            .graphicsLayer {
                scaleX = scale; scaleY = scale
                shadowElevation = shadowDp * density.density
                shape = PosterShape
                clip = true
            }
            .background(Brush.linearGradient(listOf(Color(0xFF191A1F), Color(0xFF08090B))), PosterShape)
            .onFocusChanged { state ->
                if (state.isFocused && !focused) onFocused(media)
                focused = state.isFocused
            }
            .focusable()
            .clickable { onClick(media) }
    ) {
        AsyncImage(request, media.title, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(overlay))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color.Transparent, .58f to Color.Transparent, 1f to Color.Black.copy(alpha = .92f))))
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(media.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (focused) {
                Spacer(Modifier.height(3.dp))
                Text("${media.year}  •  ${media.rating}", color = Color.White.copy(alpha = .62f), fontSize = 11.sp)
            }
        }
    }
}
