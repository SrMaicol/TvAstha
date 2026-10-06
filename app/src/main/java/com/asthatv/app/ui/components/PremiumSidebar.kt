package com.asthatv.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SidebarDestination(val id: String, val label: String, val icon: ImageVector)

val DefaultSidebarDestinations = listOf(
    SidebarDestination("home", "Inicio", Icons.Outlined.Home),
    SidebarDestination("search", "Buscar", Icons.Outlined.Search),
    SidebarDestination("library", "Biblioteca", Icons.Outlined.VideoLibrary),
    SidebarDestination("addons", "Addons", Icons.Outlined.Extension),
    SidebarDestination("downloads", "Descargas", Icons.Outlined.Download),
    SidebarDestination("settings", "Ajustes", Icons.Outlined.Settings)
)

@Composable
fun PremiumSidebar(
    selectedId: String,
    onSelected: (SidebarDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var railFocused by remember { mutableStateOf(false) }
    val width by animateDpAsState(if (railFocused) 232.dp else 84.dp, tween(210), label = "railWidth")
    val labelAlpha by animateFloatAsState(if (railFocused) 1f else 0f, tween(150), label = "labelAlpha")

    Column(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = .98f), Color.Black.copy(alpha = .86f), Color.Transparent)
                )
            )
            .padding(start = 22.dp, top = 36.dp, bottom = 28.dp)
            .onFocusChanged { railFocused = it.hasFocus },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("A", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 9.dp, bottom = 42.dp))
            DefaultSidebarDestinations.forEach { item ->
                SidebarItem(item, item.id == selectedId, labelAlpha) { onSelected(item) }
                Spacer(Modifier.height(9.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 7.dp)) {
            Box(Modifier.size(30.dp).background(Color.White.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Person, null, tint = Color.White.copy(alpha = .82f), modifier = Modifier.size(18.dp))
            }
            Text("Perfil", color = Color.White.copy(alpha = .72f), fontSize = 14.sp,
                modifier = Modifier.padding(start = 14.dp).alpha(labelAlpha), maxLines = 1)
        }
    }
}

@Composable
private fun SidebarItem(item: SidebarDestination, selected: Boolean, labelAlpha: Float, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val iconAlpha by animateFloatAsState(if (focused || selected) 1f else .58f, tween(140), label = "iconAlpha")
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, tween(160), label = "iconScale")
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp && (it.key == Key.Enter || it.key == Key.DirectionCenter)) { onClick(); true } else false
            }
            .focusable(interactionSource = remember { MutableInteractionSource() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(if (selected) 24.dp else 0.dp).background(Color.White.copy(alpha = if (selected) .92f else 0f)))
        Spacer(Modifier.width(9.dp))
        Icon(item.icon, item.label, tint = Color.White.copy(alpha = iconAlpha), modifier = Modifier.size((22 * scale).dp))
        Text(item.label, color = Color.White.copy(alpha = if (focused || selected) .94f else .68f), fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(start = 16.dp).alpha(labelAlpha), maxLines = 1)
    }
}
