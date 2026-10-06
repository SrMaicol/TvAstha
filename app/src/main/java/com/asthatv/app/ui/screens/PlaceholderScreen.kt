package com.asthatv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF15171B), Color.Black), radius = 900f)))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 138.dp)) {
            Text(title, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(subtitle, color = Color.White.copy(alpha = .58f), fontSize = 15.sp)
        }
    }
}
