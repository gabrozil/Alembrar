package com.clipboardreminder.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Conjunto de cores pré-definidas para campos e lembretes */
val PRESET_COLORS: List<Int?> = listOf(
    null, // "Nenhuma cor" → transparente / default
    0xFF4285F4.toInt(), // Azul Google
    0xFF0F9D58.toInt(), // Verde
    0xFFDB4437.toInt(), // Vermelho
    0xFFF4B400.toInt(), // Amarelo
    0xFF9C27B0.toInt(), // Roxo
    0xFFFF5722.toInt(), // Laranja
    0xFF00BCD4.toInt(), // Ciano
    0xFF795548.toInt(), // Marrom
    0xFF607D8B.toInt(), // Azul-cinza
    0xFFE91E63.toInt(), // Rosa
    0xFF8BC34A.toInt(), // Verde-limão
)

fun Int?.toComposeColor(): Color = if (this == null) Color.Transparent else Color(this)

@Composable
fun ColorPickerRow(
    selectedColor: Int?,
    onColorSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Cor do item",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(PRESET_COLORS) { color ->
                val isSelected = color == selectedColor
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                  else Color.Transparent,
                    animationSpec = spring(),
                    label = "colorPickerBorder"
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (color == null) MaterialTheme.colorScheme.surfaceVariant
                            else Color(color)
                        )
                        .border(
                            BorderStroke(2.dp, borderColor),
                            CircleShape
                        )
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (color == null) {
                        // Show "none" indicator
                        Text(
                            "–",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selecionado",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Strip colorido para ser exibido na borda esquerda de um card */
@Composable
fun ColorAccentStrip(color: Int?, modifier: Modifier = Modifier) {
    if (color != null) {
        Box(
            modifier = modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .background(Color(color))
        )
    }
}
