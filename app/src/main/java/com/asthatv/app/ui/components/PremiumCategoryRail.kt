package com.asthatv.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class TvCategory(val id: String, val label: String)

@Composable
fun PremiumCategoryRail(
    categories: List<TvCategory>,
    selectedId: String,
    onSelected: (TvCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        items(categories, key = { it.id }, contentType = { "category" }) { category ->
            var focused by remember { mutableStateOf(false) }
            val selected = selectedId == category.id
            val scale by animateFloatAsState(if (focused) 1.06f else 1f, tween(160), label = "categoryScale")
            val opacity by animateFloatAsState(if (selected || focused) 1f else .52f, tween(160), label = "categoryOpacity")

            Column(
                modifier = Modifier
                    .scale(scale)
                    .alpha(opacity)
                    .onFocusChanged { focused = it.isFocused }
                    .onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyUp && (it.key == Key.Enter || it.key == Key.DirectionCenter)) {
                            onSelected(category); true
                        } else false
                    }
                    .focusable()
                    .padding(vertical = 9.dp)
            ) {
                Text(
                    category.label,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier
                        .width(if (selected) 28.dp else if (focused) 16.dp else 0.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color.White, Color.White.copy(alpha = .20f))),
                            RoundedCornerShape(99.dp)
                        )
                )
            }
        }
    }
}
