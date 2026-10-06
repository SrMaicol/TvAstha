package com.asthatv.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asthatv.app.model.MediaItemUi
import com.asthatv.app.model.MediaSection

@Composable
fun MediaRow(
    section: MediaSection,
    onFocused: (MediaItemUi) -> Unit,
    onOpen: (MediaItemUi) -> Unit
) {
    Column {
        Text(
            section.title,
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = section.items,
                key = { it.id },
                contentType = { "poster" }
            ) { item ->
                PremiumPoster(item, onFocused, onOpen)
            }
        }
    }
}
